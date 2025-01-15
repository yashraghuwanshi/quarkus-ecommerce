package org.acme.controller;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.acme.payload.Product;
import org.acme.service.ProductServiceClient;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Slf4j
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @RestClient
    private ProductServiceClient productServiceClient;

    @GET
    public List<Product> getProducts() {
        return productServiceClient.getProducts();
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

    // Fallback method
    public Response fallbackGetProductById(Long id) {
        long fallbackStartTime = System.currentTimeMillis(); // Start time for fallback
        log.warn("Fallback invoked for product with ID: {}", id);
        Product product = new Product(id, "Unknown Product", "Fallback Description", 0.0);
        long fallbackEndTime = System.currentTimeMillis(); // End time for fallback
        log.info("Fallback executed for product with ID {} in {} ms", id, (fallbackEndTime - fallbackStartTime));
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(product).build();
    }

    @GET
    @Path("/search/{name}")
    @Timeout(250)
    public Product getProdutByName(String name) {
        randomDelay();
        return productServiceClient.getProductByName(name);
    }

    private void randomDelay() {
        try {
            Thread.sleep(new Random().nextInt(500));
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
         }
    }

}
