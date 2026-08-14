package com.aura.aura.domain.analysis.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AuraAnalysisResult {
    private String style;
    private String mood;
    @JsonProperty("energy_level")
    private Integer energyLevel;
    private List<String> palette;
}
