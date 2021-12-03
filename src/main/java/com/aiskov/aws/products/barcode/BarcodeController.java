package com.aiskov.aws.products.barcode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.krysalis.barcode4j.impl.code128.Code128Constants.CODESET_B;
import static org.springframework.http.CacheControl.noCache;
import static org.springframework.http.MediaType.IMAGE_PNG;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@Controller
@RequestMapping("/barcode")
@RequiredArgsConstructor
public class BarcodeController {

    private final BarcodeService barcodeGenerationService;

    @GetMapping("/{value}")
    public ResponseEntity<byte[]> barcode(@PathVariable String value) {
        byte[] generatedBarcode = this.barcodeGenerationService.generate(CODESET_B, value);

        HttpHeaders headers = new HttpHeaders();

        headers.setCacheControl(noCache().getHeaderValue());
        headers.setContentType(IMAGE_PNG);

        return ok().headers(headers).body(generatedBarcode);
    }
}