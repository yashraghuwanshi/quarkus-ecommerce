package org.acme.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.acme.payload.Product;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/products")
@RegisterRestClient(configKey = "post-api")
public interface ProductServiceClient {

    @POST
    Product createProduct(Product product);

    @GET
    List<Product> getProducts();

    @GET
    @Path("/{id}")
    Product getProductById(@PathParam("id") Long id);

    @GET
    @Path("/search/{name}")
    Product getProductByName(@PathParam("name") String name);

}
