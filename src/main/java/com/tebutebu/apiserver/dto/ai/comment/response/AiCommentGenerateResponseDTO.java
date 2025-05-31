package com.tebutebu.apiserver.dto.ai.comment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class AiCommentGenerateResponseDTO {

    private String message;

    private Map<String, String> data;

}
