package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.ProjectImage;
import com.tebutebu.apiserver.domain.Tag;
import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.comment.ai.request.AiCommentGenerateRequestDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectSummaryDTO;
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
import com.tebutebu.apiserver.pagination.dto.request.ContextCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorTimePageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.CursorMetaDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.TimeCursorMetaDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import com.tebutebu.apiserver.repository.CommentRepository;
import com.tebutebu.apiserver.repository.ProjectRepository;
import com.tebutebu.apiserver.repository.paging.project.ProjectPagingRepository;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
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

    private final CommentRepository commentRepository;

    private final ProjectImageService projectImageService;

    private final TagService tagService;

    private final ProjectRankingSnapshotService projectRankingSnapshotService;

    private final AiCommentRequestService aiCommentRequestService;

    private final AiBadgeImageRequestService aiBadgeImageRequestService;

    @Value("${ai.comment.default.type}")
    private String aiCommentDefaultType;

    @Override
    public ProjectResponseDTO get(Long id) {
        Project project = projectRepository.findProjectWithTeamAndImagesById(id)
                .orElseThrow(() -> new NoSuchElementException("projectNotFound"));
        return buildProjectResponseDTO(project);
    }

    @Override
    public ProjectResponseDTO getByTeamId(Long teamId) {
        Project project = projectRepository.findProjectByTeamId(teamId)
                .orElseThrow(() -> new NoSuchElementException("projectNotFound"));
        return buildProjectResponseDTO(project);
    }

    @Override
    public boolean existsByTeamId(Long teamId) {
        return projectRepository.existsByTeamId(teamId);
    }

    @Override
    public CursorPageResponseDTO<ProjectPageResponseDTO, CursorMetaDTO> getRankingPage(ContextCursorPageRequestDTO dto) {
        if (dto.getContextId() == null) {
            throw new CustomValidationException("contextIdRequired");
        }

        CursorPage<ProjectPageResponseDTO> page = projectPagingRepository.findByRankingCursor(dto);

        CursorMetaDTO meta = CursorMetaDTO.builder()
                .nextCursorId(page.nextCursorId())
                .hasNext(page.hasNext())
                .build();

        return CursorPageResponseDTO.<ProjectPageResponseDTO, CursorMetaDTO>builder()
                .data(page.items())
                .meta(meta)
                .build();
    }

    @Override
    public CursorPageResponseDTO<ProjectPageResponseDTO, TimeCursorMetaDTO> getLatestPage(CursorTimePageRequestDTO dto) {
        CursorPage<ProjectPageResponseDTO> page = projectPagingRepository.findByLatestCursor(dto);

        TimeCursorMetaDTO meta = TimeCursorMetaDTO.builder()
                .nextCursorId(page.nextCursorId())
                .nextCursorTime(page.nextCursorTime())
                .hasNext(page.hasNext())
                .build();

        return CursorPageResponseDTO.<ProjectPageResponseDTO, TimeCursorMetaDTO>builder()
                .data(page.items())
                .meta(meta)
                .build();
    }

    @Override
    public Long register(ProjectCreateRequestDTO dto) {
        if (projectRepository.existsByTeamId(dto.getTeamId())) {
            throw new CustomValidationException("projectAlreadyExists");
        }

        Project project = projectRepository.save(dtoToEntity(dto));
        Team team = project.getTeam();

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

        ProjectSummaryDTO projectSummaryDTO = ProjectSummaryDTO.builder()
                .title(project.getTitle())
                .introduction(project.getIntroduction())
                .detailedDescription(project.getDetailedDescription())
                .deploymentUrl(project.getDeploymentUrl())
                .githubUrl(project.getGithubUrl())
                .tags(tagContents)
                .teamId(dto.getTeamId())
                .term(team.getTerm())
                .teamNumber(team.getNumber())
                .build();

        AiCommentGenerateRequestDTO aiDto = AiCommentGenerateRequestDTO.builder()
                .commentType(aiCommentDefaultType)
                .projectSummary(projectSummaryDTO)
                .build();

        Long projectId = project.getId();
        aiCommentRequestService.requestAiComment(projectId, aiDto);

        aiBadgeImageRequestService.requestGenerateBadgeImage(projectSummaryDTO);

        return projectId;
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

    private ProjectResponseDTO buildProjectResponseDTO(Project project) {
        Long id = project.getId();

        Team team = project.getTeam();

        List<ProjectImageResponseDTO> images = project.getImages().stream()
                .map(projectImageService::entityToDTO)
                .collect(Collectors.toList());

        List<TagResponseDTO> tags = project.getTagContents().stream()
                .map(contentDto -> TagResponseDTO.builder()
                        .content(contentDto.getContent())
                        .build())
                .toList();

        ProjectRankingSnapshotResponseDTO snapshot = projectRankingSnapshotService.getLatestSnapshot();
        Integer teamRank = snapshot.getData().stream()
                .filter(item -> id.equals(item.getProjectId()))
                .findFirst()
                .map(RankingItemDTO::getRank)
                .orElse(null);

        long commentCount = commentRepository.findCommentCountMap(List.of(id))
                .getOrDefault(id, 0L);

        return entityToDTO(project, team, images, tags, teamRank, commentCount);
    }

}
