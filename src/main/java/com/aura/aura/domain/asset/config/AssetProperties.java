package com.aura.aura.domain.asset.config;

import com.aura.aura.domain.analysis.entity.Mood;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "aura.assets")
@Data
public class AssetProperties {
    private String baseUrl;
    private Map<String, String> hands;
    private Map<String, String> patterns;

    public String getPatternUrl(Mood mood) {
        String path = patterns.get(mood.name().toLowerCase());
        if (path == null) {
            path = patterns.get("original");
        }
        return baseUrl + path;
    }
}
