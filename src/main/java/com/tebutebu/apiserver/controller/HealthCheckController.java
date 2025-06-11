package com.tebutebu.apiserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> body = Map.of(
                "status", "running",
                "timestamp", Instant.now().toString()
        );
        return ResponseEntity.ok().body(body);
    }

}
