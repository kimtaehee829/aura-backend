package com.aura.aura.domain.output.dto.response;

import com.aura.aura.domain.product.dto.ProductResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LandingResponse {

    @JsonProperty("video_status")
    private String videoStatus;

    @JsonProperty("video_url")
    private String videoUrl;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("soul_tag")
    private SoulTagResponse soulTag;

    @JsonProperty("products")
    private List<ProductResponse> products;
}