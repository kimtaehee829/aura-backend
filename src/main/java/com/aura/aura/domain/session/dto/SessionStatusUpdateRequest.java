package com.aura.aura.domain.session.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SessionStatusUpdateRequest {

    @NotBlank(message = "status는 필수입니다.")
    private String status;
}
