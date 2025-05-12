package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.ProjectImage;
import com.tebutebu.apiserver.domain.Tag;
import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.project.image.request.ProjectImageRequestDTO;
import com.tebutebu.apiserver.dto.project.image.response.ProjectImageResponseDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectCreateRequestDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectUpdateRequestDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectPageResponseDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectResponseDTO;
import com.tebutebu.apiserver.dto.snapshot.response.ProjectRankingSnapshotResponseDTO;
import com.tebutebu.apiserver.dto.snapshot.response.RankingItemDTO;
import com.tebutebu.apiserver.dto.tag.request.TagCreateRequestDTO;
import com.tebutebu.apiserver.dto.tag.response.TagResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorMetaDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import com.tebutebu.apiserver.repository.ProjectRepository;
import com.tebutebu.apiserver.repository.paging.project.ProjectPagingRepository;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    private final ProjectPagingRepository projectPagingRepository;

    private final ProjectImageService projectImageService;

    private final TagService tagService;

    private final ProjectRankingSnapshotService projectRankingSnapshotService;

    @Override
    public ProjectResponseDTO get(Long id) {
        Project project = projectRepository.findProjectWithTeamAndImagesById(id)
                .orElseThrow(() -> new NoSuchElementException("projectNotFound"));

        Team team = project.getTeam();
        List<ProjectImageResponseDTO> images = project.getImages().stream()
                .map(projectImageService::entityToDTO)
                .collect(Collectors.toList());

        List<TagResponseDTO> tags = project.getTagContents().stream()
                .map(tagContentDTO -> TagResponseDTO.builder()
                        .content(tagContentDTO.getContent())
                        .build())
                .toList();

        ProjectRankingSnapshotResponseDTO snapshot = projectRankingSnapshotService.getLatestSnapshot();
        Integer teamRank = snapshot.getData().stream()
                .filter(item -> id.equals(item.getProjectId()))
                .findFirst()
                .map(RankingItemDTO::getRank)
                .orElse(null);

        return entityToDTO(project, team, images, tags, teamRank);
    }
  
    @Override
    public boolean existsByTeamId(Long teamId) {
        return projectRepository.existsByTeamId(teamId);
    }

    @Override
    public CursorPageResponseDTO<ProjectPageResponseDTO> getRankingPage(CursorPageRequestDTO dto) {
        if (dto.getContextId() == null) {
            throw new CustomValidationException("contextIdRequired");
        }

        CursorPage<Project> cursorPage = projectPagingRepository.findByRankingCursor(dto);

        List<ProjectPageResponseDTO> data = cursorPage.items().stream()
                .map(this::entityToPageDTO)
                .collect(Collectors.toList());

        CursorMetaDTO meta = CursorMetaDTO.builder()
                .nextCursorId(cursorPage.nextCursorId())
                .nextCursorTime(cursorPage.nextCursorTime())
                .hasNext(cursorPage.hasNext())
                .build();

        return CursorPageResponseDTO.<ProjectPageResponseDTO>builder()
                .data(data)
                .meta(meta)
                .build();
    }

    @Override
    public CursorPageResponseDTO<ProjectPageResponseDTO> getLatestPage(CursorPageRequestDTO dto) {
        CursorPage<Project> page = projectPagingRepository.findByLatestCursor(dto);

        List<ProjectPageResponseDTO> data = page.items().stream()
                .map(this::entityToPageDTO)
                .collect(Collectors.toList());

        return CursorPageResponseDTO.<ProjectPageResponseDTO>builder()
                .data(data)
                .meta(CursorMetaDTO.builder()
                        .nextCursorId(page.nextCursorId())
                        .nextCursorTime(page.nextCursorTime())
                        .hasNext(page.hasNext())
                        .build())
                .build();
    }

    @Override
    public Long register(ProjectCreateRequestDTO dto) {
        if (projectRepository.existsByTeamId(dto.getTeamId())) {
            throw new CustomValidationException("projectAlreadyExists");
        }

        Project project = projectRepository.save(dtoToEntity(dto));

        List<String> tagContents = dto.getTags().stream()
                .map(TagCreateRequestDTO::getContent)
                .distinct()
                .collect(Collectors.toList());

        List<Tag> tagEntities = tagContents.stream()
                .map(content -> {
                    Long tagId = tagService.register(project.getId(), new TagCreateRequestDTO(content));
                    return Tag.builder().id(tagId).content(content).build();
                })
                .collect(Collectors.toList());
        project.replaceTags(tagEntities);

        project.changeTagContents(tagContents);

        projectRepository.save(project);
        return project.getId();
    }

    @Override
    public void modify(Long projectId, ProjectUpdateRequestDTO dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("projectNotFound"));

        project.changeTitle(dto.getTitle());
        project.changeIntroduction(dto.getIntroduction());
        project.changeDetailedDescription(dto.getDetailedDescription());
        project.changeDeploymentUrl(dto.getDeploymentUrl());
        project.changeGithubUrl(dto.getGithubUrl());

        project.getImages().clear();
        dto.getImages().forEach(imgDto -> {
            ProjectImage pi = ProjectImage.builder()
                    .sequence(imgDto.getSequence())
                    .url(imgDto.getUrl())
                    .build();
            pi.changeProject(project);
            project.getImages().add(pi);
        });

        if (!dto.getImages().isEmpty()) {
            project.changeRepresentativeImageUrl(dto.getImages().getFirst().getUrl());
        }

        List<String> tagContents = dto.getTags().stream()
                .map(TagCreateRequestDTO::getContent)
                .distinct()
                .collect(Collectors.toList());

        List<Tag> tags = tagContents.stream()
                .map(content -> {
                    Long tagId = tagService.register(projectId, new TagCreateRequestDTO(content));
                    return Tag.builder().id(tagId).content(content).build();
                })
                .collect(Collectors.toList());
        project.replaceTags(tags);

        project.changeTagContents(tagContents);
        projectRepository.save(project);
    }

    @Override
    public void delete(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new NoSuchElementException("projectNotFound");
        }
        projectRepository.deleteById(projectId);
    }

    @Override
    public Project dtoToEntity(ProjectCreateRequestDTO dto) {
        Project project = Project.builder()
                .team(Team.builder().id(dto.getTeamId()).build())
                .title(dto.getTitle())
                .introduction(dto.getIntroduction())
                .detailedDescription(dto.getDetailedDescription())
                .deploymentUrl(dto.getDeploymentUrl())
                .githubUrl(dto.getGithubUrl())
                .build();

        for (ProjectImageRequestDTO img : dto.getImages()) {
            ProjectImage pi = ProjectImage.builder()
                    .sequence(img.getSequence())
                    .url(img.getUrl())
                    .build();
            pi.changeProject(project);
            project.getImages().add(pi);
        }

        if (!dto.getImages().isEmpty()) {
            project.changeRepresentativeImageUrl(dto.getImages().getFirst().getUrl());
        }

        return project;
    }

    private ProjectPageResponseDTO entityToPageDTO(Project project) {
        Team team = project.getTeam();
        List<TagResponseDTO> tags = project.getTagContents().stream()
                .map(tagContentDTO -> TagResponseDTO.builder()
                        .content(tagContentDTO.getContent())
                        .build())
                .toList();
        return ProjectPageResponseDTO.builder()
                .id(project.getId())
                .teamId(team.getId())
                .term(team.getTerm())
                .teamNumber(team.getNumber())
                .givedPumatiCount(team.getGivedPumatiCount())
                .receivedPumatiCount(team.getReceivedPumatiCount())
                .badgeImageUrl(team.getBadgeImageUrl())
                .title(project.getTitle())
                .introduction(project.getIntroduction())
                .representativeImageUrl(project.getRepresentativeImageUrl())
                .tags(tags)
                .createdAt(project.getCreatedAt())
                .modifiedAt(project.getModifiedAt())
                .build();
    }

}
