package com.aura.aura.domain.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionAbandonResponse {

    @JsonProperty("public_id")
    private String publicId;

    private String status;

    @JsonProperty("abandoned_at_status")
    private String abandonedAtStatus;
}
