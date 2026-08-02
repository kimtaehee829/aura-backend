package com.aura.aura.domain.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SessionResponse {

    @JsonProperty("public_id")
    private String publicId;

    private String status;

    @JsonProperty("bag_product")
    private BagProductDto bagProduct;

    private AuraDto aura;

    @JsonProperty("started_at")
    private LocalDateTime startedAt;

    @Getter
    @Builder
    public static class BagProductDto {
        @JsonProperty("product_id")
        private Long productId;
        private String name;
    }

    @Getter
    @Builder
    public static class AuraDto {
        private String style;
        private String mood;
        @JsonProperty("energy_level")
        private String energyLevel;
        private List<String> palette;
    }
}
