package com.tebutebu.apiserver.dto.oauth.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class OAuthCreateRequestDTO {

    @NotNull(message = ValidationMessages.MEMBER_ID_REQUIRED)
    @Positive(message = ValidationMessages.MEMBER_ID_MUST_BE_POSITIVE)
    private Long memberId;

    @NotBlank(message = ValidationMessages.PROVIDER_REQUIRED)
    @Size(max = 20, message = ValidationMessages.PROVIDER_MAX_LENGTH_EXCEEDED)
    private String provider;

    @NotBlank(message = ValidationMessages.PROVIDER_ID_REQUIRED)
    @Size(max = 50, message = ValidationMessages.PROVIDER_ID_MAX_LENGTH_EXCEEDED)
    private String providerId;

}
