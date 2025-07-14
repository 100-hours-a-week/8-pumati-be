package com.tebutebu.apiserver.dto.project.image.request;

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
public class ProjectImageRequestDTO {

    @NotNull(message = ValidationMessages.PROJECT_ID_REQUIRED)
    @Positive(message = ValidationMessages.PROJECT_ID_MUST_BE_POSITIVE)
    private Long projectId;

    @NotBlank(message = ValidationMessages.PROJECT_IMAGE_URL_REQUIRED)
    @Size(max = 512, message = ValidationMessages.PROJECT_IMAGE_URL_MAX_LENGTH_EXCEEDED)
    private String url;

    @NotNull(message = ValidationMessages.PROJECT_IMAGE_SEQUENCE_REQUIRED)
    private Integer sequence;

}
