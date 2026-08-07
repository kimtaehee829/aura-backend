package com.aura.aura.domain.output.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OutputResponse {

    @JsonProperty("video_status")
    private String videoStatus;

    @JsonProperty("soul_tag_url")
    private String soulTagUrl;

    @JsonProperty("qr_image_url")
    private String qrImageUrl;

    @JsonProperty("landing_url")
    private String landingUrl;

}