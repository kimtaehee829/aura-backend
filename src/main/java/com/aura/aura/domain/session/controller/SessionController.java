package com.aura.aura.domain.session.controller;

import com.aura.aura.domain.session.dto.*;
import com.aura.aura.domain.session.service.SessionService;
import com.aura.aura.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SessionCreateResponse> createSession(@Valid @RequestBody SessionCreateRequest request) {
        return ApiResponse.ok(sessionService.createSession(request));
    }

    @PatchMapping("/{publicId}/status")
    public ApiResponse<SessionStatusUpdateResponse> updateStatus(
            @PathVariable String publicId,
            @Valid @RequestBody SessionStatusUpdateRequest request) {
        return ApiResponse.ok(sessionService.updateStatus(publicId, request));
    }

    @PostMapping("/{publicId}/abandon")
    public ApiResponse<SessionAbandonResponse> abandonSession(@PathVariable String publicId) {
        return ApiResponse.ok(sessionService.abandonSession(publicId));
    }

    @GetMapping("/{publicId}")
    public ApiResponse<SessionResponse> getSession(@PathVariable String publicId) {
        return ApiResponse.ok(sessionService.getSession(publicId));
    }
}
