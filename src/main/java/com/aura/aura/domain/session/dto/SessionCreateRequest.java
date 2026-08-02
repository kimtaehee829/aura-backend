package com.aura.aura.domain.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SessionCreateRequest {

    @NotNull(message = "store_id는 필수입니다.")
    @JsonProperty("store_id")
    private Long storeId;

    @NotNull(message = "consent_agreed는 필수입니다.")
    @JsonProperty("consent_agreed")
    private Boolean consentAgreed;
}
