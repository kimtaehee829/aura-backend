package com.aura.aura.domain.output.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class FinalizeOutputResponse {

    @JsonProperty("landing_url")
    private String landingUrl;

    @JsonProperty("qr_image_url")
    private String qrImageUrl;

    @JsonProperty("soul_tag_url")
    private String soulTagUrl;

    @JsonProperty("video_status")
    private String videoStatus;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

}