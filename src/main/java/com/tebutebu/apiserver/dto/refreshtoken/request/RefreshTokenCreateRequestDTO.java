package com.tebutebu.apiserver.dto.refreshtoken.request;

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

    @NotNull(message = "회원 ID는 필수 입력 값입니다.")
    @Positive(message = "회원 ID는 양수여야 합니다.")
    private Long memberId;

    @NotBlank(message = "토큰 값은 필수 입력 값입니다.")
    private String token;

    @NotNull(message = "만료일시는 필수 입력 값입니다.")
    private LocalDateTime expiresAt;

}
