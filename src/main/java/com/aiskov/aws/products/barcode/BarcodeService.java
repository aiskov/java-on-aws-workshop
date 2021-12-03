package com.aiskov.aws.products.barcode;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import static java.text.Normalizer.Form.NFD;
import static java.text.Normalizer.normalize;

@Slf4j
@Service
public class BarcodeService {
    private static final int DPI = 600;

    @SneakyThrows
    public byte[] generate(int codeSetB, String value) {
        String cleanValue = normalize(value, NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("\\W+", "-")
                .replaceAll("^-", "")
                .replaceAll("-$", "");

        Code128Bean code128Bean = new Code128Bean();

        code128Bean.setModuleWidth(UnitConv.in2mm(4.5f / DPI));
        code128Bean.setCodeset(codeSetB);
        code128Bean.doQuietZone(false);
        code128Bean.setBarHeight(12d);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                    out, "image/x-png", DPI, TYPE_BYTE_BINARY, false, 0);

            code128Bean.generateBarcode(canvas, cleanValue);

            canvas.finish();

            return out.toByteArray();
        }
    }
}