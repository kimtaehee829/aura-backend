package com.aura.aura.domain.asset.controller;

import com.aura.aura.domain.asset.config.AssetProperties;
import com.aura.aura.domain.asset.dto.AssetManifestResponse;
import com.aura.aura.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetProperties assetProperties;

    @GetMapping("/manifest")
    public ResponseEntity<ApiResponse<AssetManifestResponse>> getManifest() {
        AssetManifestResponse manifest = new AssetManifestResponse(assetProperties);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(ApiResponse.ok(manifest));
    }
}
