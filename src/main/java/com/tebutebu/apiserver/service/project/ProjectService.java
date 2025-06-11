package com.tebutebu.apiserver.service.project;

import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.project.image.response.ProjectImageResponseDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectCreateRequestDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectUpdateRequestDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectGithubUrlDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectPageResponseDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectResponseDTO;
import com.tebutebu.apiserver.dto.tag.response.TagResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorTimePageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.CursorMetaDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.TimeCursorMetaDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ProjectService {

    @Transactional(readOnly = true)
    ProjectResponseDTO get(Long id);

    @Transactional(readOnly = true)
    boolean existsByTeamId(Long teamId);

    @Transactional(readOnly = true)
    ProjectResponseDTO getByTeamId(Long teamId);

    @Transactional(readOnly = true)
    CursorPageResponseDTO<ProjectPageResponseDTO, CursorMetaDTO> getRankingPage(ContextCursorPageRequestDTO dto);

    @Transactional(readOnly = true)
    CursorPageResponseDTO<ProjectPageResponseDTO, TimeCursorMetaDTO> getLatestPage(CursorTimePageRequestDTO dto);

    @Transactional(readOnly = true)
    List<ProjectGithubUrlDTO> getAllGithubUrls();

    Long register(ProjectCreateRequestDTO dto);

    void modify(Long projectId, ProjectUpdateRequestDTO dto);

    void delete(Long projectId);

    Project dtoToEntity(ProjectCreateRequestDTO dto);

    default ProjectResponseDTO entityToDTO(Project project, Team team, List<ProjectImageResponseDTO> images, List<TagResponseDTO> tags, Integer teamRank, Long commentCount) {
        return ProjectResponseDTO.builder()
                .id(project.getId())
                .teamId(team.getId())
                .teamRank(teamRank)
                .term(team.getTerm())
                .teamNumber(team.getNumber())
                .commentCount(commentCount)
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
