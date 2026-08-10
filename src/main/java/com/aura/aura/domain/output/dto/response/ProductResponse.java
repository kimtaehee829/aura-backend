package com.aura.aura.domain.output.dto.response;

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
    private Integer price;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("purchase_url")
    private String purchaseUrl;
}