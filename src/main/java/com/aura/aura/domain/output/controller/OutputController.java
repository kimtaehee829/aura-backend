package com.aura.aura.domain.output.controller;

import com.aura.aura.domain.output.dto.request.VideoCompleteRequest;
import com.aura.aura.domain.output.dto.response.*;
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
            @PathVariable String publicId
    ) {
        return ApiResponse.ok(outputService.finalizeOutput(publicId));
    }

    @PostMapping("/video-upload-url")
    public ApiResponse<VideoUploadUrlResponse> generateVideoUploadUrl(
            @PathVariable String publicId
    ) {
        return ApiResponse.ok(outputService.generateVideoUploadUrl(publicId));
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

    @PostMapping("/thumbnail-upload-url")
    public ApiResponse<ThumbnailUploadUrlResponse> getThumbnailUploadUrl(
            @PathVariable String publicId
    ) {
        return ApiResponse.ok(outputService.getThumbnailUploadUrl(publicId));
    }
}