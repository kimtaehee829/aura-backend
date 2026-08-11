package com.aura.aura.domain.landing.controller;

import com.aura.aura.domain.landing.dto.request.LandingEventRequest;
import com.aura.aura.domain.landing.service.LandingService;
import com.aura.aura.domain.output.dto.response.LandingResponse;
import com.aura.aura.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/landing/{publicId}")
@RequiredArgsConstructor
public class LandingController {

    private final LandingService landingService;

    @GetMapping
    public ApiResponse<LandingResponse> getLanding(
            @PathVariable String publicId
    ) {
        return ApiResponse.ok(landingService.getLanding(publicId));
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createLandingEvent(
            @PathVariable String publicId,
            @Valid @RequestBody LandingEventRequest request
    ) {
        landingService.createLandingEvent(publicId, request);
        return ApiResponse.ok();
    }
}