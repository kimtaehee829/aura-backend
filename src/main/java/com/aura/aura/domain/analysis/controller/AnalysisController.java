package com.aura.aura.domain.analysis.controller;

import com.aura.aura.domain.analysis.dto.AnalysisCreateRequest;
import com.aura.aura.domain.analysis.dto.AnalysisResponse;
import com.aura.aura.domain.analysis.service.AnalysisService;
import com.aura.aura.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/analysis/preview")
    public ApiResponse<AnalysisResponse> previewAnalysis(
            @Valid @RequestBody AnalysisCreateRequest request) {
        
        AnalysisResponse response = analysisService.previewAnalysis(request.getImageBase64());
        return ApiResponse.ok(response);
    }

    @PostMapping("/sessions/{publicId}/analysis")
    public ApiResponse<AnalysisResponse> createAnalysis(
            @PathVariable("publicId") String publicId,
            @Valid @RequestBody AnalysisCreateRequest request) {
        
        AnalysisResponse response = analysisService.createAnalysis(publicId, request.getImageBase64());
        return ApiResponse.ok(response);
    }
}
