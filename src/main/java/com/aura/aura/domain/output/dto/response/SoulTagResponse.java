package com.aura.aura.domain.output.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SoulTagResponse {
    private String qrUrl;
    private String landingUrl;
    private String thumbnailUrl;
}