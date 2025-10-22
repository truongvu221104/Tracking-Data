package com.example.demo.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/api/public/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "service", "demo", "stage", "chặng-0");
    }
}
