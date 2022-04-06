package com.aiskov.aws.products.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.LinkedList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

@Getter
@Setter
@Entity
@ToString
public class Product {
    @Id
    private String id;

    private String name;

    @OneToMany(mappedBy = "product", cascade = ALL, fetch = EAGER)
    private List<ProductFile> files;

    public List<ProductFile> getFiles() {
        if (this.files == null) this.files = new LinkedList<>();
        return this.files;
    }
}
