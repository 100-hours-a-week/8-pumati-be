package com.tebutebu.apiserver.service.chatbot.stream;

import com.tebutebu.apiserver.util.ChatbotSseClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChatbotStreamServiceImpl implements ChatbotStreamService {

    private final ChatbotSseClient chatbotSseClient;

    private final Map<String, Sinks.Many<ServerSentEvent<String>>> sinks = new ConcurrentHashMap<>();

    @Override
    public Flux<ServerSentEvent<String>> connect(Long projectId, String sessionId) {

        String key = buildKey(projectId, sessionId);

        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
        sinks.put(key, sink);

        sendStreamStart(projectId, sessionId);

        chatbotSseClient.startChatStream(
                projectId,
                sessionId,
                (ev, data) -> forwardEvent(key, ev, data),
                error -> handleErrorAndClose(projectId, sessionId, error),
                () -> disconnect(projectId, sessionId)
        );

        return sink.asFlux();
    }

    @Override
    public void disconnect(Long projectId, String sessionId) {
        String key = buildKey(projectId, sessionId);

        sendStreamEnd(projectId, sessionId);

        Sinks.Many<?> sink = sinks.remove(key);
        if (sink == null) return;

        sink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        log.info("Local stream closed: {}", key);

        chatbotSseClient.stopChatStream(projectId, sessionId);
        log.info("Remote stream closed: {}", key);
    }

    @Override
    public boolean isSessionActive(Long projectId, String sessionId) {
        return sinks.containsKey(buildKey(projectId, sessionId));
    }

    @Override
    public void sendStreamStart(Long projectId, String sessionId) {
        forwardEvent(buildKey(projectId, sessionId), "stream-start", "connected");
    }

    @Override
    public void sendStreamEnd(Long projectId, String sessionId) {
        forwardEvent(buildKey(projectId, sessionId), "stream-end", "disconnected");
    }

    @Override
    public void sendErrorEvent(Long projectId, String sessionId, String error) {
        forwardEvent(buildKey(projectId, sessionId), "error", error);
    }

    private void forwardEvent(String key, String event, String data) {

        Sinks.Many<ServerSentEvent<String>> sink = sinks.get(key);
        if (sink == null) {
            log.warn("No active sink for {}", key);
            return;
        }

        ServerSentEvent<String> sse = ServerSentEvent.<String>builder()
                .event(event)
                .data(data)
                .build();

        if (sink.tryEmitNext(sse).isFailure()) {
            log.warn("Failed to emit [{}] for {}", event, key);
        }
    }

    private void handleErrorAndClose(Long projectId, String sessionId, Throwable error) {
        String key = buildKey(projectId, sessionId);
        log.error("Relay error for {}: {}", key, error.getMessage(), error);
        sendErrorEvent(projectId, sessionId, "internalServerError");
        disconnect(projectId, sessionId);
    }

    private String buildKey(Long projectId, String sessionId) {
        return projectId + ":" + sessionId;
    }

}
