package com.aura.aura.domain.product.controller;

import com.aura.aura.domain.product.dto.ProductResponse;
import com.aura.aura.domain.product.service.ProductService;
import com.aura.aura.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<List<ProductResponse>> getProducts(
            @RequestParam(value = "type", required = false) String type) {
        
        List<ProductResponse> products = productService.getProducts(type);
        return ApiResponse.ok(products);
    }
}
