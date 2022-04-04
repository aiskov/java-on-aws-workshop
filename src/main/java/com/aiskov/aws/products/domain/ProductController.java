package com.aiskov.aws.products.domain;

import com.aiskov.aws.products.files.S3Downloaded;
import com.aiskov.aws.products.files.S3FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

import static org.springframework.http.MediaType.parseMediaType;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private static final String HOST_NAME = System.getenv("HOSTNAME");

    private final S3FileStorage s3FileStorage;
    private final ProductService productService;

    private final Environment environment;

    @GetMapping("/")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("list.html");
        modelAndView.addObject("products", this.productService.getProducts());
        modelAndView.addObject("version", this.environment.getProperty("app.version", "-"));
        modelAndView.addObject("hostname", HOST_NAME);

        return modelAndView;
    }

    @PostMapping("/")
    public String save(@RequestParam("name") String name) {
        Product product = new Product();
        product.setName(name);

        this.productService.addProduct(product);

        return "redirect:/";
    }

    @PostMapping("/{productId}/files")
    public String upload(@PathVariable String productId, @RequestParam("file") MultipartFile file) throws IOException {
        this.s3FileStorage.upload(file.getOriginalFilename(), file.getInputStream(), file.getSize());
        this.productService.addFile(productId, file.getOriginalFilename());
        return "redirect:/";
    }

    @GetMapping("/files/{name}")
    public ResponseEntity<InputStreamResource> files(@PathVariable String name) throws IOException {
        S3Downloaded downloaded = this.s3FileStorage.retrieve(name);

        return ResponseEntity.ok()
                .contentType(parseMediaType(downloaded.getContentType()))
                .body(new InputStreamResource(downloaded.getData()));
    }

    @GetMapping("/files/{name}/share")
    public ResponseEntity<String> share(@PathVariable String name) {

        String url = this.s3FileStorage.presignUrl(name);

        return ResponseEntity.ok()
                .body(url);
    }

}
