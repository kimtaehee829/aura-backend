package com.aura.aura.domain.product.dto;

import com.aura.aura.domain.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {
    
    @JsonProperty("product_id")
    private Long productId;
    
    @JsonProperty("product_type")
    private String productType;
    
    private String name;
    
    private String category;
    
    private Integer price;
    
    @JsonProperty("image_url")
    private String imageUrl;
    
    @JsonProperty("model_url")
    private String modelUrl;
    
    @JsonProperty("purchase_url")
    private String purchaseUrl;

    public static ProductResponse fromEntity(Product product) {
        return ProductResponse.builder()
                .productId(product.getId())
                .productType(product.getProductType())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .modelUrl(product.getModelUrl())
                .purchaseUrl(product.getPurchaseUrl())
                .build();
    }
}
