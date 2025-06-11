package com.tebutebu.apiserver.service.chatbot.message;

public interface ChatbotMessageService {

    boolean handleMessage(Long projectId, String sessionId, String content);

}
