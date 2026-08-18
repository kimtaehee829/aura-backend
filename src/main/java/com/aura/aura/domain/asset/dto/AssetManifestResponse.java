package com.aura.aura.domain.asset.dto;

import com.aura.aura.domain.analysis.entity.Mood;
import com.aura.aura.domain.asset.config.AssetProperties;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class AssetManifestResponse {
    private final Map<String, String> hands;
    private final Map<String, String> patterns;

    public AssetManifestResponse(AssetProperties properties) {
        String baseUrl = properties.getBaseUrl();
        
        this.hands = new HashMap<>();
        if (properties.getHands() != null) {
            properties.getHands().forEach((k, v) -> this.hands.put(k, baseUrl + v));
        }

        this.patterns = new HashMap<>();
        if (properties.getPatterns() != null) {
            this.patterns.put("original", baseUrl + properties.getPatterns().get("original"));
            
            for (Mood mood : Mood.values()) {
                this.patterns.put(mood.name(), properties.getPatternUrl(mood));
            }
        }
    }
}
