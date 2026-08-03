package com.aura.aura.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnalysisCreateRequest {
    
    @NotBlank(message = "이미지 base64 값은 필수입니다.")
    @JsonProperty("image_base64")
    private String imageBase64;
}
