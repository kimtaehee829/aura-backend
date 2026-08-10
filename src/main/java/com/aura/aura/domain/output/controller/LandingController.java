package com.aura.aura.domain.output.controller;

import com.aura.aura.domain.output.dto.response.LandingResponse;
import com.aura.aura.domain.output.service.OutputService;
import com.aura.aura.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions/{publicId}/outputs")
@RequiredArgsConstructor
public class LandingController {

    private final OutputService outputService;

    @GetMapping("/landing")
    public ApiResponse<LandingResponse> getLanding(
            @PathVariable String publicId
    ) {
        return ApiResponse.ok(outputService.getLanding(publicId));
    }
}