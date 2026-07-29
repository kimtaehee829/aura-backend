package com.aura.aura.domain.common;

import com.aura.aura.global.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("AURA Backend Server is Running.");
    }
}