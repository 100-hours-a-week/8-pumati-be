package com.tebutebu.apiserver.service.chatbot.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotSessionServiceImpl implements ChatbotSessionService {

    @Override
    public String createSession(Long projectId) {
        return UUID.randomUUID().toString();
    }

}
