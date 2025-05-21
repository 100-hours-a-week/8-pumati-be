package com.tebutebu.apiserver.dto.oauth.response;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class OAuthResponseDTO {

    private Long memberId;

    private String provider;

    private String providerId;

}
