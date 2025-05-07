package com.tebutebu.apiserver.dto.project.response;

import com.tebutebu.apiserver.dto.project.image.response.ProjectImageResponseDTO;
import com.tebutebu.apiserver.dto.tag.response.TagResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProjectResponseDTO {

    private Long id;

    private Long teamId;

    private String title;

    private String introduction;

    private String detailedDescription;

    private String representativeImageUrl;

    private List<ProjectImageResponseDTO> images;

    private String deploymentUrl;

    private String githubUrl;

    private List<TagResponseDTO> tags;

    private Long givedPumatiCount;

    private Long receivedPumatiCount;

    private String badgeImageUrl;

    private LocalDateTime createdAt, modifiedAt;

}
