package com.aura.aura.domain.output.controller;

import com.aura.aura.domain.output.dto.response.SoulTagResponse;
import com.aura.aura.domain.output.service.OutputService;
import com.aura.aura.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/soul-tag")
public class SoulTagController {

    private final OutputService outputService;

    @GetMapping("/{publicId}")
    public ApiResponse<SoulTagResponse> getSoulTag(
            @PathVariable String publicId
    ) {
        return ApiResponse.ok(outputService.getSoulTag(publicId));
    }
}