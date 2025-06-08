package com.tebutebu.apiserver.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Log4j2
@Component
public class ChatbotSseClient {

    @Value("${chatbot.service.base-url}")
    private String chatbotServiceBaseUrl;

    private WebClient webClient;

    @PostConstruct
    public void initClient() {
        this.webClient = WebClient.builder()
                .baseUrl(chatbotServiceBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    public void startChatStream(Long projectId, String sessionId,
                                BiConsumer<String, String> onEvent,
                                Consumer<Throwable> onError,
                                Runnable onComplete) {
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{projectId}/chatbot/sessions/{sessionId}/stream")
                        .build(projectId, sessionId))
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .doOnNext(event -> {
                    onEvent.accept(event.event(), event.data());
                })
                .doOnError(onError)
                .doOnComplete(onComplete)
                .subscribe();
    }

    public void sendMessage(Long projectId, String sessionId, Map<String, Object> body,
                            Consumer<String> onNext,
                            Consumer<Throwable> onError,
                            Runnable onComplete) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{projectId}/chatbot/sessions/{sessionId}/message")
                        .build(projectId, sessionId))
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(onNext)
                .doOnError(onError)
                .doOnComplete(onComplete)
                .subscribe();
    }

    public void stopChatStream(Long projectId, String sessionId) {
        webClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/projects/{projectId}/chatbot/sessions/{sessionId}/stream")
                        .build(projectId, sessionId))
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(res -> log.debug("Remote stream disconnected: sessionId={}", sessionId))
                .doOnError(e -> log.warn("Remote disconnection failed: {}", e.getMessage()))
                .onErrorResume(WebClientResponseException.NotFound.class, e -> Mono.empty())
                .subscribe();
    }

}
