package com.aiskov.aws.products.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Data
@Entity
public class ProductFile {
    @Id
    private String name;

    @ManyToOne
    @JoinColumn
    private Product product;
}
