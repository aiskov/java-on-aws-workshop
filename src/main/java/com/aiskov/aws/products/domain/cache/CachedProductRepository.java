package com.aiskov.aws.products.domain.cache;

import org.springframework.data.repository.CrudRepository;

public interface CachedProductRepository extends CrudRepository<CachedProduct, String> {
}
