package com.tebutebu.apiserver.dto.project.request;

import com.tebutebu.apiserver.dto.project.image.request.ProjectImageRequestDTO;
import com.tebutebu.apiserver.dto.tag.request.TagCreateRequestDTO;
import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateRequestDTO {

    @NotNull(message = ValidationMessages.TEAM_ID_REQUIRED)
    private Long teamId;

    @NotBlank(message = ValidationMessages.PROJECT_TITLE_REQUIRED)
    @Size(max = 64, message = ValidationMessages.PROJECT_TITLE_MAX_LENGTH_EXCEEDED)
    private String title;

    @Size(max = 150, message = ValidationMessages.PROJECT_INTRODUCTION_MAX_LENGTH_EXCEEDED)
    private String introduction;

    @Size(max = 1000, message = ValidationMessages.PROJECT_DETAILED_DESCRIPTION_MAX_LENGTH_EXCEEDED)
    private String detailedDescription;

    @Size(max = 512, message = ValidationMessages.PROJECT_DEPLOYMENT_URL_MAX_LENGTH_EXCEEDED)
    private String deploymentUrl;

    @Size(max = 512, message = ValidationMessages.PROJECT_GITHUB_URL_MAX_LENGTH_EXCEEDED)
    private String githubUrl;

    @NotNull(message = ValidationMessages.PROJECT_TAGS_REQUIRED)
    @Size(min = 1, max = 5, message = ValidationMessages.PROJECT_TAGS_SIZE_OUT_OF_BOUNDS)
    private List<@Valid TagCreateRequestDTO> tags;

    @NotNull(message = ValidationMessages.PROJECT_IMAGES_REQUIRED)
    @Size(min = 1, message = ValidationMessages.PROJECT_IMAGES_MIN_SIZE_REQUIRED)
    private List<ProjectImageRequestDTO> images;

}
