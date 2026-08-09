package com.aura.aura.domain.output.controller;

import com.aura.aura.domain.output.dto.*;
import com.aura.aura.domain.output.dto.FinalizeOutputResponse;
import com.aura.aura.domain.output.dto.request.FinalizeOutputRequest;
import com.aura.aura.domain.output.dto.request.VideoCompleteRequest;
import com.aura.aura.domain.output.dto.request.VideoUploadUrlRequest;
import com.aura.aura.domain.output.dto.response.OutputResponse;
import com.aura.aura.domain.output.dto.response.VideoCompleteResponse;
import com.aura.aura.domain.output.dto.response.VideoUploadUrlResponse;
import com.aura.aura.domain.output.service.OutputService;
import com.aura.aura.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions/{publicId}/outputs")
public class OutputController {

    private final OutputService outputService;

    @PostMapping("/finalize")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FinalizeOutputResponse> finalizeOutput(
            @PathVariable String publicId,
            @Valid @RequestBody FinalizeOutputRequest request
    ) {
        return ApiResponse.ok(outputService.finalizeOutput(publicId, request));
    }

    @PostMapping("/video-url")
    public ApiResponse<VideoUploadUrlResponse> generateVideoUploadUrl(
            @PathVariable String publicId,
            @Valid @RequestBody VideoUploadUrlRequest request
    ) {
        return ApiResponse.ok(outputService.generateVideoUploadUrl(publicId, request));
    }

    @PostMapping("/video-complete")
    public ApiResponse<VideoCompleteResponse> completeVideo(
            @PathVariable String publicId,
            @Valid @RequestBody VideoCompleteRequest request
    ) {
        return ApiResponse.ok(outputService.completeVideo(publicId, request));
    }

    @GetMapping
    public ApiResponse<OutputResponse> getOutput(
            @PathVariable String publicId
    ) {
        return ApiResponse.ok(outputService.getOutput(publicId));
    }

    @PostMapping("/videos/fail")
    public ApiResponse<Void> failVideo(
            @PathVariable String publicId
    ) {
        outputService.failVideo(publicId);
        return ApiResponse.ok();
    }
}