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

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRotateRequestDTO {

    @NotNull(message = ValidationMessages.MEMBER_ID_REQUIRED)
    @Positive(message = ValidationMessages.MEMBER_ID_MUST_BE_POSITIVE)
    private Long memberId;

    @NotBlank(message = ValidationMessages.OLD_TOKEN_REQUIRED)
    private String oldToken;

    @NotNull(message = ValidationMessages.NEW_EXPIRY_MINUTES_REQUIRED)
    @Positive(message = ValidationMessages.NEW_EXPIRY_MINUTES_MUST_BE_POSITIVE)
    private Integer newExpiryMinutes;

}
