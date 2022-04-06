package com.aiskov.aws.products.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Entity
@ToString(exclude = "product")
public class ProductFile {
    @Id
    private String name;

    @ManyToOne
    @JoinColumn
    private Product product;
}
