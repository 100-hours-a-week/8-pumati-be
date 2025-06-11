package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.service.chatbot.session.ChatbotSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/chatbot")
public class ChatbotSessionController {

    private final ChatbotSessionService sessionService;

    @PostMapping("/session")
    public ResponseEntity<Map<String, Object>> createSession(@PathVariable Long projectId) {
        String sessionId = sessionService.createSession(projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "sessionIdCreated",
                "data", sessionId
        ));
    }

}
