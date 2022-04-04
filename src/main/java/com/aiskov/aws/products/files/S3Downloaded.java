package com.aiskov.aws.products.files;

import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

@Value
@Builder
public class S3Downloaded {
    String name;
    String contentType;
    InputStream data;
}
