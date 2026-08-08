package com.aura.aura.domain.output.dto.response;

import com.aura.aura.domain.output.enums.VideoStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VideoCompleteResponse {

    @JsonProperty("video_status")
    private VideoStatus videoStatus;
}