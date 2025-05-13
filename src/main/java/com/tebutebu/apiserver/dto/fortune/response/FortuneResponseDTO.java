package com.tebutebu.apiserver.dto.fortune.response;

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
