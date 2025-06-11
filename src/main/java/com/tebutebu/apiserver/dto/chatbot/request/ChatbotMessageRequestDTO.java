package com.tebutebu.apiserver.dto.chatbot.request;

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

    @NotBlank(message = "메시지 내용은 필수 입력 값입니다.")
    @Size(max = 50, message = "메시지 내용은 최대 50자까지 가능합니다.")
    private String content;

}
