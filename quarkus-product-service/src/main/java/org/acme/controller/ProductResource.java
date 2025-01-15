package org.acme.controller;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acme.entity.Product;
import org.acme.repository.ProductRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final ProductRepository productRepository;

    @POST
    @Transactional
    public Product createProduct(Product product){
        product.persist();
        return product;
    }

    @GET
    public List<Product> getProducts(){
        return Product.listAll();
    }

    @GET
    @Path("/{id}")
    public Product getProductById(Long id){
        log.info("Trying to fetch product with ID: {}", id);
        return Product.findById(id);
    }

    @GET
    @Path("/search/{name}")
    public Product getProductByName(String name){
        return productRepository.findByName(name);
    }

    @GET
    @Path("/products/count")
    public Long countProducts(){
        return Product.count();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Product updateProduct(Long id, Product product){
        Product existingProduct = Product.findById(id);

        if(existingProduct == null){
            throw new NotFoundException("Product not found with id: " + id);
        }

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());

        return existingProduct;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void deleteProduct(Long id){
        Product.deleteById(id);
    }
}
