package com.aura.aura.domain.interaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InteractionCreateResponse(
        @JsonProperty("saved_count")
        int savedCount
) {
}
