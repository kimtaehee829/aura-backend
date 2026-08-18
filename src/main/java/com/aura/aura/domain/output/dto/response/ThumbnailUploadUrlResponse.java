package com.aura.aura.domain.output.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ThumbnailUploadUrlResponse {

    private String uploadUrl;
    private String objectPath;
    private int expiresInSeconds;
}