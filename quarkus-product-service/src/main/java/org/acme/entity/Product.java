package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product extends PanacheEntity {

    private String name;
    private String description;
    private double price;

    // find the product by name
    public static Product findByName(String name){
        return find("name", name).firstResult();
    }

    // find products ordered by names
    public static List<Product> findOrdered(){
        return find("ORDER BY name").list();
    }
}