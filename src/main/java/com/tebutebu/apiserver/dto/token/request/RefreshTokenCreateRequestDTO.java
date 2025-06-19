package com.tebutebu.apiserver.dto.token.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class RefreshTokenCreateRequestDTO {

    @NotNull(message = ValidationMessages.MEMBER_ID_REQUIRED)
    @Positive(message = ValidationMessages.MEMBER_ID_MUST_BE_POSITIVE)
    private Long memberId;

    @NotBlank(message = ValidationMessages.TOKEN_REQUIRED)
    private String token;

    @NotNull(message = ValidationMessages.EXPIRES_AT_REQUIRED)
    private LocalDateTime expiresAt;

}
