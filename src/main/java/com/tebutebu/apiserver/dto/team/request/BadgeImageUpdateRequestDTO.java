package com.tebutebu.apiserver.dto.team.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
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

    @NotBlank(message = ValidationMessages.BADGE_IMAGE_URL_REQUIRED)
    @Size(max = 512, message = ValidationMessages.BADGE_IMAGE_URL_MAX_LENGTH_EXCEEDED)
    private String badgeImageUrl;

}
