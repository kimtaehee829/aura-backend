package com.aura.aura.domain.product.dto;

import com.aura.aura.domain.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccessoryResponse {

    @JsonProperty("product_id")
    private Long productId;

    private String name;

    @JsonProperty("slot_order")
    private Integer slotOrder;

    @JsonProperty("model_url")
    private String modelUrl;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("is_attached")
    private Boolean isAttached;

    public static AccessoryResponse of(Product product, Integer slotOrder, Boolean isAttached) {
        return AccessoryResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .slotOrder(slotOrder)
                .modelUrl(product.getModelUrl())
                .imageUrl(product.getImageUrl())
                .isAttached(isAttached)
                .build();
    }
}
