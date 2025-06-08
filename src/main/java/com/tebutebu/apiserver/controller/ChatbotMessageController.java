package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.chatbot.request.ChatbotMessageRequestDTO;
import com.tebutebu.apiserver.service.chatbot.message.ChatbotMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/chatbot/sessions/{sessionId}")
@RequiredArgsConstructor
public class ChatbotMessageController {

    private final ChatbotMessageService messageService;

    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable Long projectId,
            @PathVariable String sessionId,
            @Valid @RequestBody ChatbotMessageRequestDTO request
    ) {
        boolean success = messageService.handleMessage(projectId, sessionId, request.getContent());

        if (!success) {
            return ResponseEntity.status(404).body(Map.of("message", "sessionNotFound"));
        }

        return ResponseEntity.accepted().body(Map.of("message", "messageAccepted"));
    }

}
