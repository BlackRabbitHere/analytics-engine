package com.analytics.analytics_engine.controller;


import com.analytics.analytics_engine.dto.ProductRequestDTO;
import com.analytics.analytics_engine.dto.ProductResponseDTO;
import com.analytics.analytics_engine.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create-product")
    public ResponseEntity<ProductResponseDTO>createProduct(@RequestBody @Valid ProductRequestDTO productRequestDTO){

        return new ResponseEntity<>(productService.createProduct(productRequestDTO),HttpStatus.CREATED);
    }
}
