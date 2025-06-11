package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.service.chatbot.stream.ChatbotStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/chatbot/sessions/{sessionId}")
public class ChatbotStreamController {

    private final ChatbotStreamService streamService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> connect(
            @PathVariable Long projectId,
            @PathVariable String sessionId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Flux.just(
                    ServerSentEvent.<String>builder()
                            .event("error")
                            .data("invalidToken")
                            .build(),
                    ServerSentEvent.<String>builder()
                            .event("done")
                            .data("ok")
                            .build()
            );
        }

        return streamService.connect(projectId, sessionId);
    }

    @DeleteMapping("/stream")
    public ResponseEntity<?> disconnect(@PathVariable Long projectId, @PathVariable String sessionId) {
        streamService.disconnect(projectId, sessionId);
        return ResponseEntity.ok(Map.of("message", "disconnected"));
    }

}
