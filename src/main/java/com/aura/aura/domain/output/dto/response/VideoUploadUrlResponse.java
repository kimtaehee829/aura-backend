package com.aura.aura.domain.output.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class VideoUploadUrlResponse {

    @JsonProperty("upload_url")
    private String uploadUrl;

    @JsonProperty("object_path")
    private String objectPath;

    @JsonProperty("expires_in_seconds")
    private Integer expiresInSeconds;

}
