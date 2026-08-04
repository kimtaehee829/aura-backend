package com.aura.aura.domain.product.repository;

import com.aura.aura.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByProductType(String productType);
}
