package com.aura.aura.domain.output.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
@AllArgsConstructor
public class VideoUploadUrlResponse {

    @JsonProperty("upload_url")
    private String uploadUrl;

    @JsonProperty("object_path")
    private String objectPath;

    @JsonProperty("thumbnail_upload_url")
    private String thumbnailUploadUrl;

    @JsonProperty("thumbnail_object_path")
    private String thumbnailObjectPath;

    @JsonProperty("expires_in_seconds")
    private Integer expiresInSeconds;

}
