package org.acme.controller;

import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.acme.payload.Product;
import org.acme.service.ProductServiceClient;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Slf4j
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @RestClient
    private ProductServiceClient productServiceClient;

    @CircuitBreaker(
            requestVolumeThreshold = 4,
            failureRatio = 0.5,
            delay = 5,
            delayUnit = ChronoUnit.SECONDS
    )
    @POST
    public Response createProduct(Product product) {
        Product savedProduct = productServiceClient.createProduct(product);
        return Response.status(Response.Status.CREATED).entity(savedProduct).build();
    }

    @RateLimit(value = 2, window = 10, windowUnit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "fallbackGetProducts")
    @GET
    public Response getProducts() {
        List<Product> products = productServiceClient.getProducts();
        return Response.ok(products).build();
    }

    @Retry(maxRetries = 2, delay = 2, delayUnit = ChronoUnit.MILLIS)
    @Fallback(fallbackMethod = "fallbackGetProductById")
    @GET
    @Path("/{id}")
    public Response getProdutById(Long id) {
        long startTime = System.currentTimeMillis();
        log.info("Trying to fetch product with ID: {}", id);
        Product product = productServiceClient.getProductById(id);

        if (product == null) {
            log.warn("Product with ID {} not found", id);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        long endTime = System.currentTimeMillis();
        log.info("Successfully fetched product with ID {} in {} ms", id, (endTime - startTime));
        return Response.status(Response.Status.OK).entity(product).build();
    }

    @GET
    @Path("/search/{name}")
    @Timeout(250)
    @Fallback(fallbackMethod = "fallbackGetProductByName")
    public Response getProdutByName(String name) {
        Product product = productServiceClient.getProductByName(name);
        return Response.ok(product).build() ;
    }

    // Fallback method for create product
    public Response fallbackCreateProduct(Product product){
        Product fallbackProduct = new Product(null, product.getName(), "Fallback Description", 0.0);
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(fallbackProduct).build();
    }

    // Fallback method for get products
    // Throws RateLimitException exception
    public Response fallbackGetProducts() {
        log.warn("Rate limit exceeded. Returning an empty product list.");
        return Response.status(Response.Status.TOO_MANY_REQUESTS).entity(Collections.emptyList()).build();
    }

    // Fallback method for get product by id
    public Response fallbackGetProductById(Long id) {
        long fallbackStartTime = System.currentTimeMillis();
        log.warn("Fallback invoked for product with ID: {}", id);
        Product fallbackProduct = new Product(id, "Unknown Product", "Fallback Description", 0.0);
        long fallbackEndTime = System.currentTimeMillis();
        log.info("Fallback executed for product with ID {} in {} ms", id, (fallbackEndTime - fallbackStartTime));
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(fallbackProduct).build();
    }

    // Fallback method for get product by name
    // Throws TimeoutException
    public Response fallbackGetProductByName(String name){
        log.warn("Timeout occured. Please try again later.");
        return Response.status(Response.Status.REQUEST_TIMEOUT).build();
    }

}
