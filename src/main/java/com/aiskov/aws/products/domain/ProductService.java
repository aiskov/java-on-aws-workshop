package com.aiskov.aws.products.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public void addProduct(Product product) {
        product.setId(UUID.randomUUID().toString());
        this.productRepository.save(product);
    }

    public List<Product> getProducts() {
       return this.productRepository.findAll();
    }

    public void addFile(String productId, String name) {
        Product product = this.productRepository.findById(productId).orElseThrow();
        ProductFile file = new ProductFile();
        file.setName(name);
        file.setProduct(product);
        product.getFiles().add(file);
    }
}
