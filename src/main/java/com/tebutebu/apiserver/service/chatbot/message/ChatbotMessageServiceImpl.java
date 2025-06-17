package com.tebutebu.apiserver.service.chatbot.message;

import com.tebutebu.apiserver.service.chatbot.stream.ChatbotStreamService;
import com.tebutebu.apiserver.util.ChatbotSseClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChatbotMessageServiceImpl implements ChatbotMessageService {

    private final ChatbotStreamService streamService;

    private final ChatbotSseClient chatbotSseClient;

    @Override
    public boolean handleMessage(Long projectId, String sessionId, String content) {

        if (!streamService.isSessionActive(projectId, sessionId)) {
            log.warn("Inactive session: projectId={}, sessionId={}", projectId, sessionId);
            return false;
        }

        log.info("Handling chatbot message: projectId={}, sessionId={}, content={}", projectId, sessionId, content);

        chatbotSseClient.sendMessage(
                projectId,
                sessionId,
                Map.of("content", content),
                ack -> log.debug("ChatbotSvc ACK: sessionId={}, ack={}", sessionId, ack),
                error -> {
                    String reason = resolveErrorReason(error);
                    streamService.sendErrorEvent(projectId, sessionId, reason);
                },
                () -> log.debug("ChatbotSvc ACK: sessionId={}", sessionId)
        );

        return true;
    }

    private String resolveErrorReason(Throwable error) {

        if (error instanceof WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "sessionNotFound";
            }
        }
        return "internalServerError";
    }

}
