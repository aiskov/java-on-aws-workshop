package com.aiskov.aws.products.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Data
@Entity
public class Product {
    @Id
    private String id;

    private String name;

    @OneToMany(mappedBy = "product", cascade = ALL)
    private List<ProductFile> files;
}
