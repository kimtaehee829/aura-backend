package com.aura.aura.domain.analysis.controller;

import com.aura.aura.domain.analysis.dto.AnalysisCreateRequest;
import com.aura.aura.domain.analysis.dto.AnalysisResponse;
import com.aura.aura.domain.analysis.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/analysis/preview")
    public ResponseEntity<AnalysisResponse> previewAnalysis(
            @Valid @RequestBody AnalysisCreateRequest request) {
        
        AnalysisResponse response = analysisService.previewAnalysis(request.getImageBase64());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{publicId}/analysis")
    public ResponseEntity<AnalysisResponse> createAnalysis(
            @PathVariable("publicId") String publicId,
            @Valid @RequestBody AnalysisCreateRequest request) {
        
        AnalysisResponse response = analysisService.createAnalysis(publicId, request.getImageBase64());
        return ResponseEntity.ok(response);
    }
}
