package com.tebutebu.apiserver.dto.team.request;

import jakarta.validation.constraints.NotBlank;
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
public class BadgeImageUpdateRequestDTO {

    @NotBlank(message = "뱃지 이미지 URL은 필수입니다.")
    @Size(max = 512, message = "뱃지 이미지 URL은 최대 512자까지 허용됩니다.")
    private String badgeImageUrl;

}
