package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.project.image.response.ProjectImageResponseDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectCreateRequestDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectUpdateRequestDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectResponseDTO;
import com.tebutebu.apiserver.dto.tag.response.TagResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
public interface ProjectService {

    @Transactional(readOnly = true)
    ProjectResponseDTO get(Long id);

    CursorPageResponseDTO<ProjectResponseDTO> getRankingPage(CursorPageRequestDTO dto);

    Long register(ProjectCreateRequestDTO dto);

    void modify(Long projectId, ProjectUpdateRequestDTO dto);

    void delete(Long projectId);

    Project dtoToEntity(ProjectCreateRequestDTO dto);

    default ProjectResponseDTO entityToDTO(Project project, Team team, List<ProjectImageResponseDTO> images) {

        List<TagResponseDTO> tags = project.getTagContents().stream()
                .map(content -> TagResponseDTO.builder()
                        .content(content)
                        .build())
                .collect(Collectors.toList());

        return ProjectResponseDTO.builder()
                .id(project.getId())
                .teamId(team.getId())
                .term(team.getTerm())
                .teamNumber(team.getNumber())
                .givedPumatiCount(team.getGivedPumatiCount())
                .receivedPumatiCount(team.getReceivedPumatiCount())
                .badgeImageUrl(team.getBadgeImageUrl())
                .title(project.getTitle())
                .introduction(project.getIntroduction())
                .detailedDescription(project.getDetailedDescription())
                .representativeImageUrl(project.getRepresentativeImageUrl())
                .images(images)
                .deploymentUrl(project.getDeploymentUrl())
                .githubUrl(project.getGithubUrl())
                .tags(tags)
                .createdAt(project.getCreatedAt())
                .modifiedAt(project.getModifiedAt())
                .build();
    }

}
