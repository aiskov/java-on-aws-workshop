package com.aiskov.aws.products.domain.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@Data
@Builder
@RedisHash
@NoArgsConstructor
@AllArgsConstructor
public class CachedProduct {
    private String id;
    private String name;
    private List<CachedProductFile> files;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CachedProductFile {
        private String name;
    }
}
