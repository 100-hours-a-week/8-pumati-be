package com.tebutebu.apiserver.dto.token.response;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenResponseDTO {

    private Long id;

    private Long memberId;

    private String token;

    private LocalDateTime expiresAt;

}
