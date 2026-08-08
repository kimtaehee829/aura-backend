package com.aura.aura.domain.output.dto.response;

import com.aura.aura.domain.output.entity.SessionOutput;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

@Getter
@Builder
@Transactional(readOnly = true)
public class OutputResponse {

    @JsonProperty("video_status")
    private String videoStatus;

    @JsonProperty("soul_tag_url")
    private String soulTagUrl;

    @JsonProperty("qr_image_url")
    private String qrImageUrl;

    @JsonProperty("landing_url")
    private String landingUrl;

    public static OutputResponse from(SessionOutput output) {
        return OutputResponse.builder()
                .videoStatus(output.getVideoStatus().name())
                .soulTagUrl(output.getSoulTagUrl())
                .qrImageUrl(output.getQrImageUrl())
                .landingUrl(output.getLandingUrl())
                .build();
    }
}