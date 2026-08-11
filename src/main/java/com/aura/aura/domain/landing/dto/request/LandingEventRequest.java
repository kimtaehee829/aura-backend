package com.aura.aura.domain.landing.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LandingEventRequest {

    @NotBlank(message = "event_type은 필수입니다.")
    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("product_id")
    private Long productId;
}
