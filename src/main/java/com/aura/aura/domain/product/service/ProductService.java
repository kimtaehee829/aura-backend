package com.aura.aura.domain.product.service;

import com.aura.aura.domain.product.dto.ProductResponse;
import com.aura.aura.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> getProducts(String type) {
        if (type != null && !type.isBlank()) {
            return productRepository.findAllByProductType(type.toUpperCase())
                    .stream()
                    .map(ProductResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
