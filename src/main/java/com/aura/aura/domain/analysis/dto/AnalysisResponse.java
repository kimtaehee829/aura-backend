package com.aura.aura.domain.analysis.dto;

import com.aura.aura.domain.analysis.entity.AuraAnalysis;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AnalysisResponse {
    
    private String style;
    private String mood;
    
    @JsonProperty("energy_level")
    private String energyLevel;
    
    private List<String> palette;
    
    @JsonProperty("pattern_url")
    private String patternUrl;
    
    @JsonProperty("latency_ms")
    private int latencyMs;
    
    @JsonProperty("fallback")
    private boolean fallback;

    public static AnalysisResponse fromEntity(AuraAnalysis entity, String patternUrl) {
        return AnalysisResponse.builder()
                .style(entity.getStyle())
                .mood(entity.getMood())
                .energyLevel(entity.getEnergyLevel())
                .palette(List.of(entity.getPalette1(), entity.getPalette2(), entity.getPalette3()))
                .patternUrl(patternUrl)
                .latencyMs(entity.getLatencyMs())
                .fallback(entity.getIsFallback())
                .build();
    }
}
