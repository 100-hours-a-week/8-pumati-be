package com.tebutebu.apiserver.dto.chatbot.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatbotMessageRequestDTO {

    @NotBlank(message = ValidationMessages.CHATBOT_CONTENT_REQUIRED)
    @Size(max = 50, message = ValidationMessages.COMMENT_CONTENT_MAX_LENGTH_EXCEEDED)
    private String content;

}
