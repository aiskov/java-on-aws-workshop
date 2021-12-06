package com.aiskov.aws.products.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.http.MediaType.parseMediaType;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private static final String HOST_NAME = System.getenv("HOSTNAME");

    private final ProductService productService;

    @Value("${app.files.location}")
    private String filesLocation;

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("list.html");
        modelAndView.addObject("products", this.productService.getProducts());
        modelAndView.addObject("version", this.appVersion);
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
        file.transferTo(Path.of(filesLocation, file.getOriginalFilename()));
        this.productService.addFile(productId, file.getOriginalFilename());
        return "redirect:/";
    }

    @GetMapping("/files/{name}")
    public ResponseEntity<FileSystemResource> files(@PathVariable String name) throws IOException {
        Path fileLocation = Path.of(filesLocation, name);

        return ResponseEntity.ok()
                .contentType(parseMediaType(Files.probeContentType(fileLocation)))
                .body(new FileSystemResource(fileLocation));
    }
}
