package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Product;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    // find the product by name
    public Product findByName(String name){
        return find("name", name).firstResult();
    }
}
