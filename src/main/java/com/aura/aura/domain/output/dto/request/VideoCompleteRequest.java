package com.aura.aura.domain.output.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class VideoCompleteRequest {

    @NotBlank(message = "object_path는 필수입니다.")
    @JsonProperty("object_path")
    private String objectPath;

    @NotNull(message = "duration_ms는 필수입니다.")
    @JsonProperty("duration_ms")
    private Integer durationMs;
}