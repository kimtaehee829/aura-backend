package com.aura.aura.domain.interaction.controller;

import com.aura.aura.domain.interaction.service.AdminInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/tasks")
@RequiredArgsConstructor
public class AdminInteractionController {

    private final AdminInteractionService adminInteractionService;

    @Value("${app.admin-key}")
    private String adminKey;

    @PostMapping("/generate-weekly-report")
    public ResponseEntity<Map<String, String>> triggerWeeklyReportGeneration(@RequestParam("key") String key) {
        if (!adminKey.equals(key)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String gcsUrl = adminInteractionService.generateAndUploadWeeklyReport();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Weekly report successfully generated and uploaded to GCS.");
        response.put("downloadUrl", gcsUrl);

        return ResponseEntity.ok(response);
    }
}
