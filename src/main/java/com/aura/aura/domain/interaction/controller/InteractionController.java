package com.aura.aura.domain.interaction.controller;

import com.aura.aura.domain.interaction.dto.InteractionCreateRequest;
import com.aura.aura.domain.interaction.dto.InteractionCreateResponse;
import com.aura.aura.domain.interaction.service.InteractionService;
import com.aura.aura.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions/{publicId}/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InteractionCreateResponse> saveInteractions(
            @PathVariable("publicId") String publicId,
            @Valid @RequestBody InteractionCreateRequest request) {
        
        InteractionCreateResponse response = interactionService.saveInteractions(publicId, request);
        return ApiResponse.ok(response);
    }
}
