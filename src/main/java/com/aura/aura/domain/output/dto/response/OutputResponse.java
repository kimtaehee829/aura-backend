package com.aura.aura.domain.output.dto.response;

import com.aura.aura.domain.output.entity.SessionOutput;
import com.aura.aura.domain.output.enums.VideoStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OutputResponse {

    @JsonProperty("video_status")
    private VideoStatus videoStatus;

    @JsonProperty("video_url")
    private String videoUrl;

    @JsonProperty("video_duration_ms")
    private Integer videoDurationMs;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("soul_tag_url")
    private String soulTagUrl;

    @JsonProperty("qr_image_url")
    private String qrImageUrl;

    @JsonProperty("landing_url")
    private String landingUrl;

    public static OutputResponse from(SessionOutput output) {
        return OutputResponse.builder()
                .videoStatus(output.getVideoStatus())
                .videoUrl(output.getVideoUrl())
                .videoDurationMs(output.getVideoDurationMs())
                .thumbnailUrl(output.getThumbnailUrl())
                .soulTagUrl(output.getSoulTagUrl())
                .qrImageUrl(output.getQrImageUrl())
                .landingUrl(output.getLandingUrl())
                .build();
    }
}