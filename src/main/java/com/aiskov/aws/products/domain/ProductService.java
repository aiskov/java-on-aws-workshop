package com.aiskov.aws.products.domain;

import com.aiskov.aws.products.domain.cache.CachedProduct;
import com.aiskov.aws.products.domain.cache.CachedProduct.CachedProductFile;
import com.aiskov.aws.products.domain.cache.CachedProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CachedProductRepository cachedProductRepository;

    @PostConstruct
    public void init() {
        try {
            if (this.cachedProductRepository.count() == 0) {
                this.cachedProductRepository.saveAll(this.productRepository.findAll().stream()
                        .map(this::mapToCachedProduct)
                        .collect(toList()));
            }
        } catch (Exception exp) {
            log.warn("Unable load");
        }
    }

    public void addProduct(Product product) {
        product.setId(UUID.randomUUID().toString());
        this.productRepository.save(product);
        this.cachedProductRepository.save(this.mapToCachedProduct(product));
    }

    public List<CachedProduct> getProducts() {
        List<CachedProduct> cachedProducts = this.retrieveProductFromCache();
        if (cachedProducts != null && ! cachedProducts.isEmpty()) return cachedProducts;
        return this.retrieveProduct();
    }

    public void addFile(String productId, String name) {
        Product product = this.productRepository.findById(productId).orElseThrow();
        ProductFile file = new ProductFile();
        file.setName(name);
        file.setProduct(product);
        product.getFiles().add(file);

        this.cachedProductRepository.save(this.mapToCachedProduct(product));
    }

    // Return empty optional in case of failure
    private List<CachedProduct> retrieveProductFromCache() {
        try {
            return convertToList(this.cachedProductRepository.findAll());
        } catch (Exception exp) {
            return List.of();
        }
    }

    private List<CachedProduct> retrieveProduct() {
        return this.productRepository.findAll().stream()
                .map(this::mapToCachedProduct)
                .collect(toList());
    }

    private CachedProduct mapToCachedProduct(Product product) {
        return CachedProduct.builder()
                .id(product.getId())
                .name(product.getName())
                .files(product.getFiles().stream()
                        .map(file -> CachedProductFile.builder()
                                .name(file.getName())
                                .build())
                        .collect(toList()))
                .build();
    }

    private List<CachedProduct> convertToList(Iterable<CachedProduct> cachedProducts) {
        return StreamSupport.stream(cachedProducts.spliterator(), false).collect(toList());
    }
}
