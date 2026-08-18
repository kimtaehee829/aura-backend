package com.aura.aura.domain.output.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SoulTagResponse {

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("bag_name")
    private String bagName;

    @JsonProperty("aura_code")
    private List<String> auraCode;

    @JsonProperty("mood")
    private String mood;

    @JsonProperty("styling")
    private String styling;

    @JsonProperty("forged_at")
    private String forgedAt;

    @JsonProperty("date")
    private String date;
}