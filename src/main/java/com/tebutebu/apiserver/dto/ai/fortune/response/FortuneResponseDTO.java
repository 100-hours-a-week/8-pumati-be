package com.tebutebu.apiserver.dto.ai.fortune.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FortuneResponseDTO {

    private String message;

    private DevLuckDTO data;

}
