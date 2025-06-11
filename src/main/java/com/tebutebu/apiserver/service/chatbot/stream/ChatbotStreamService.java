package com.tebutebu.apiserver.service.chatbot.stream;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface ChatbotStreamService {

    Flux<ServerSentEvent<String>> connect(Long projectId, String sessionId);

    void disconnect(Long projectId, String sessionId);

    boolean isSessionActive(Long projectId, String sessionId);

    void sendStreamStart(Long projectId, String sessionId);

    void sendStreamEnd(Long projectId, String sessionId);

    void sendErrorEvent(Long projectId, String sessionId, String error);

}
