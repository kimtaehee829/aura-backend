package com.aura.aura.domain.product.controller;

import com.aura.aura.domain.product.dto.AccessoryAttachResponse;
import com.aura.aura.domain.product.dto.AccessoryResponse;
import com.aura.aura.domain.product.service.AccessoryService;
import com.aura.aura.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions/{publicId}/accessories")
@RequiredArgsConstructor
public class AccessoryController {

    private final AccessoryService accessoryService;

    @GetMapping
    public ApiResponse<List<AccessoryResponse>> getSessionAccessories(
            @PathVariable("publicId") String publicId) {
        
        List<AccessoryResponse> responses = accessoryService.getSessionAccessories(publicId);
        return ApiResponse.ok(responses);
    }

    @PostMapping("/{productId}/attach")
    public ApiResponse<AccessoryAttachResponse> attachAccessory(
            @PathVariable("publicId") String publicId,
            @PathVariable("productId") Long productId) {
        
        AccessoryAttachResponse response = accessoryService.attachAccessory(publicId, productId);
        return ApiResponse.ok(response);
    }
}
