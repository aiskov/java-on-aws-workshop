package com.aiskov.aws.products;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class ProductsDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductsDemoApplication.class, args);
	}

}
