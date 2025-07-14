package com.tebutebu.apiserver.dto.ai.badge.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeImageModificationRequestDTO {

    private List<@NotBlank(message = ValidationMessages.EACH_TAG_MUST_NOT_BE_BLANK) String> modificationTags;

}
