package com.aura.aura.domain.output.controller;

import com.aura.aura.domain.output.dto.response.SoulTagResponse;
import com.aura.aura.domain.output.service.OutputService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/soul-tag")
public class SoulTagController {

    private final OutputService outputService;

    @GetMapping("/{publicId}")
    public SoulTagResponse getSoulTag(@PathVariable String publicId) {
        return outputService.getSoulTag(publicId);
    }
}