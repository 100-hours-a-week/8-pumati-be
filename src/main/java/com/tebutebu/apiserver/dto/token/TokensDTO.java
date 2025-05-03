package com.tebutebu.apiserver.dto.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokensDTO {

    private String accessToken;

    private String refreshToken;

}
