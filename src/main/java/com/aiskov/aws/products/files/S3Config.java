package com.aiskov.aws.products.files;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static software.amazon.awssdk.regions.Region.EU_WEST_1;

@Configuration
public class S3Config {

    @Bean
    S3Client s3Client() {
        return S3Client.builder()
                .region(EU_WEST_1)
                .build();
    }

    @Bean
    S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(EU_WEST_1)
                .build();
    }
}
