package com.aura.aura.domain.output.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class VideoUrlRequest {

    @NotBlank(message = "content_type은 필수입니다.")
    @JsonProperty("content_type")
    private String contentType;

    @NotNull(message = "duration_ms는 필수입니다.")
    @JsonProperty("duration_ms")
    private Integer durationMs;

    @NotNull(message = "include_thumbnail은 필수입니다.")
    @JsonProperty("include_thumbnail")
    private Boolean includeThumbnail;
}
