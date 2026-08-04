package com.aura.aura.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AccessoryAttachResponse {

    @JsonProperty("product_id")
    private Long productId;

    private String name;

    @JsonProperty("is_attached")
    private Boolean isAttached;

    @JsonProperty("attached_at")
    private LocalDateTime attachedAt;
}
