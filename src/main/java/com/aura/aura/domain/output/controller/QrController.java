package com.aura.aura.domain.output.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class QrController {

    @GetMapping("/qr/{fileName}")
    public ResponseEntity<Resource> getQr(@PathVariable String fileName) throws Exception {

        Path path = Paths.get(System.getProperty("user.dir") + "/qr/" + fileName);
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok().body(resource);
    }
}