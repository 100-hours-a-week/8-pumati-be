package com.tebutebu.apiserver.dto.oauth.request;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "회원 ID는 필수 입력 값입니다.")
    @Positive(message = "회원 ID는 양수여야 합니다.")
    private Long memberId;

    @NotBlank(message = "제공자는 필수 입력 값입니다.")
    @Size(max = 20, message = "제공자는 최대 20자까지 가능합니다.")
    private String provider;

    @NotBlank(message = "제공자 ID는 필수 입력 값입니다.")
    @Size(max = 50, message = "제공자 ID는 최대 50자까지 가능합니다.")
    private String providerId;

}
