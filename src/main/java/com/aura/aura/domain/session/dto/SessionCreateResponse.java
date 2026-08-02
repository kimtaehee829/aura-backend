package com.aura.aura.domain.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SessionCreateResponse {

    @JsonProperty("public_id")
    private String publicId;

    private String status;

    @JsonProperty("bag_product")
    private BagProductDto bagProduct;

    @JsonProperty("started_at")
    private LocalDateTime startedAt;

    @Getter
    @AllArgsConstructor
    public static class BagProductDto {
        @JsonProperty("product_id")
        private Long productId;
        private String name;
        @JsonProperty("model_url")
        private String modelUrl;
        @JsonProperty("image_url")
        private String imageUrl;
    }
}
