package com.aiskov.aws.products.files;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3FileStorage {
    private static final String BUCKET = "workshop-5466573423167";
    private static final String PREFIX = "products/";

    private final S3Client client;
    private final S3Presigner presigner;

    public void upload(String filename, InputStream data, long size) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(BUCKET)
                .key(PREFIX + filename)
                .build();

        this.client.putObject(request, RequestBody.fromInputStream(data, size));
    }

    @SneakyThrows
    public S3Downloaded retrieve(String file) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(BUCKET)
                .key(PREFIX + file)
                .build();

        ResponseInputStream<GetObjectResponse> result = this.client.getObject(request);

        return S3Downloaded.builder()
                .name(file)
                .contentType(result.response().contentType())
                .data(result)
                .build();
    }

    @Cacheable(value = "share-urls")
    public String presignUrl(String file) {
        log.info("Generation link for {}", file);

        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(BUCKET)
                        .key(PREFIX + file)
                        .build())
                .signatureDuration(Duration.ofDays(1))
                .build();

        return this.presigner.presignGetObject(request).url().toString();
    }
}
