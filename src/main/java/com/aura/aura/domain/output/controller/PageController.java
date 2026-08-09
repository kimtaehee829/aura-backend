package com.aura.aura.domain.output.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @GetMapping("/landing/{publicId}")
    public String landing(@PathVariable String publicId, Model model) {
        model.addAttribute("publicId", publicId);
        return "landing";
    }
}