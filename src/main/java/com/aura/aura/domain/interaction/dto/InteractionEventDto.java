package com.aura.aura.domain.interaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionEventDto {

    @NotNull
    private Integer seq;

    @NotNull
    private String phase;

    @NotNull
    @JsonProperty("target_type")
    private String targetType;

    @JsonProperty("target_part")
    private String targetPart;

    @JsonProperty("target_product_id")
    private Long targetProductId;

    @NotNull
    private String gesture;

    @NotNull
    @JsonProperty("dwell_ms")
    private Integer dwellMs;

    @JsonProperty("rotation_degrees")
    private Integer rotationDegrees;

    @NotNull
    @JsonProperty("is_completed")
    private Boolean isCompleted;

    @NotNull
    @JsonProperty("elapsed_ms")
    private Integer elapsedMs;

    @NotNull
    @JsonProperty("occurred_at")
    private LocalDateTime occurredAt;
}
