package com.aura.aura.domain.interaction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InteractionCreateRequest(
        @NotNull
        @Valid
        @Size(max = 200, message = "이벤트는 한 번에 200건까지 전송할 수 있습니다.")
        List<InteractionEventDto> events
) {
}
