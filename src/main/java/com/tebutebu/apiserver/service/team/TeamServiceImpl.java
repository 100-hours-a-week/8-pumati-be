package com.tebutebu.apiserver.service.team;

import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.domain.TeamBadgeStat;
import com.tebutebu.apiserver.dto.ai.badge.request.BadgeImageModificationRequestDTO;
import com.tebutebu.apiserver.dto.ai.badge.request.TeamBadgeImageUpdateRequestDTO;
import com.tebutebu.apiserver.dto.ai.badge.response.TeamBadgeStatPageResponseDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectSummaryDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectResponseDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.RankingItemDTO;
import com.tebutebu.apiserver.dto.tag.response.TagResponseDTO;
import com.tebutebu.apiserver.dto.team.request.TeamCreateRequestDTO;
import com.tebutebu.apiserver.dto.team.response.TeamListResponseDTO;
import com.tebutebu.apiserver.dto.team.response.TeamResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCountCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.CountCursorMetaDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import com.tebutebu.apiserver.repository.TeamBadgeStatRepository;
import com.tebutebu.apiserver.repository.TeamRepository;
import com.tebutebu.apiserver.repository.paging.badge.TeamBadgeStatPagingRepository;
import com.tebutebu.apiserver.service.ai.badge.AiBadgeImageRequestService;
import com.tebutebu.apiserver.service.project.snapshot.ProjectRankingSnapshotService;
import com.tebutebu.apiserver.service.project.ProjectService;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    @Value("${default.badge.image.url}")
    private String defaultBadgeImageUrl;

    private final TeamRepository teamRepository;

    private final TeamBadgeStatRepository teamBadgeStatRepository;

    private final TeamBadgeStatPagingRepository teamBadgeStatPagingRepository;

    private final ProjectService projectService;

    private final ProjectRankingSnapshotService projectRankingSnapshotService;

    private final AiBadgeImageRequestService aiBadgeImageRequestService;

    @Override
    public TeamResponseDTO get(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("teamNotFound"));
        Long projectId = findTeamProjectId(id);
        if (projectId == null) {
            return entityToDTO(team, null, null);
        } else {
            Integer rank = findTeamProjectRank(id);
            return entityToDTO(team, projectId, rank);
        }
    }

    @Override
    public TeamResponseDTO getByTermAndNumber(Integer term, Integer number) {
        if (term == null && number == null) {
            return null;
        }
        Team team = teamRepository.findByTermAndNumber(term, number)
                .orElseThrow(() -> new NoSuchElementException("teamNotFound"));
        Long teamId = team.getId();
        Long projectId = findTeamProjectId(teamId);
        if (projectId == null) {
            return entityToDTO(team, null, null);
        } else {
            Integer rank = findTeamProjectRank(teamId);
            return entityToDTO(team, projectId, rank);
        }
    }

    @Override
    public List<TeamListResponseDTO> getAllTeams() {
        List<Team> all = teamRepository.findAll();
        if (all.isEmpty()) {
            throw new NoSuchElementException("noTeamNumbersAvailable");
        }

        Map<Integer, List<Integer>> grouped = all.stream()
                .collect(Collectors.groupingBy(
                        Team::getTerm,
                        TreeMap::new,
                        Collectors.mapping(
                                Team::getNumber,
                                Collectors.collectingAndThen(
                                        Collectors.toSet(),
                                        set -> set.stream().sorted().toList()
                                )
                        )
                ));

        return grouped.entrySet().stream()
                .map(e -> TeamListResponseDTO.builder()
                        .term(e.getKey())
                        .teamNumbers(e.getValue())
                        .build())
                .toList();
    }

    @Override
    public Long register(TeamCreateRequestDTO dto) {

        if (teamRepository.existsByTermAndNumber(dto.getTerm(), dto.getNumber())) {
            throw new CustomValidationException("teamAlreadyExists");
        }

        Team team = dtoToEntity(dto);
        team.changeBadgeImageUrl(defaultBadgeImageUrl);
        return teamRepository.save(team).getId();
    }

    @Override
    public CursorPageResponseDTO<TeamBadgeStatPageResponseDTO, CountCursorMetaDTO> getReceivedBadgesPage(ContextCountCursorPageRequestDTO req) {
        CursorPage<TeamBadgeStatPageResponseDTO> page = teamBadgeStatPagingRepository.findByAcquiredCountCursor(req);

        CountCursorMetaDTO meta = CountCursorMetaDTO.builder()
                .nextCursorId(page.nextCursorId())
                .nextCount(page.nextCursorCount())
                .hasNext(page.hasNext())
                .build();

        return CursorPageResponseDTO.<TeamBadgeStatPageResponseDTO, CountCursorMetaDTO>builder()
                .data(page.items())
                .meta(meta)
                .build();
    }

    @Override
    public void increaseOrCreateBadge(Long giverTeamId, Long receiverTeamId) {
        TeamBadgeStat teamBadgeStat = teamBadgeStatRepository.findByGiverTeamIdAndReceiverTeamId(giverTeamId, receiverTeamId)
                .orElseGet(() -> TeamBadgeStat.builder()
                        .giverTeam(Team.builder().id(giverTeamId).build())
                        .receiverTeam(Team.builder().id(receiverTeamId).build())
                        .acquiredCount(0)
                        .build());

        teamBadgeStat.incrementAcquiredCount();
        teamBadgeStatRepository.save(teamBadgeStat);
    }


    @Override
    public void requestUpdateBadgeImage(Long teamId, BadgeImageModificationRequestDTO badgeImageModificationRequestDTO) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("teamNotFound"));

        if (team.isAiBadgeInProgress()) {
            throw new CustomValidationException("badgeModificationInProgress");
        }

        ProjectResponseDTO project = projectService.getByTeamId(teamId);
        if (project == null) {
            throw new NoSuchElementException("projectNotFound");
        }

        List<String> tagContents = project.getTags().stream()
                .map(TagResponseDTO::getContent)
                .toList();

        ProjectSummaryDTO projectSummaryDTO = ProjectSummaryDTO.builder()
                .title(project.getTitle())
                .introduction(project.getIntroduction())
                .detailedDescription(project.getDetailedDescription())
                .deploymentUrl(project.getDeploymentUrl())
                .githubUrl(project.getGithubUrl())
                .tags(tagContents)
                .teamId(teamId)
                .term(team.getTerm())
                .teamNumber(team.getNumber())
                .build();

        TeamBadgeImageUpdateRequestDTO teamBadgeImageUpdateRequestDTO = TeamBadgeImageUpdateRequestDTO.builder()
                .modificationTags(badgeImageModificationRequestDTO.getModificationTags())
                .projectSummary(projectSummaryDTO)
                .build();

        boolean isAiBadgeInProgress = aiBadgeImageRequestService.requestUpdateBadgeImage(teamBadgeImageUpdateRequestDTO);
        team.setAiBadgeInProgress(isAiBadgeInProgress);
    }

    @Override
    public void updateBadgeImageUrl(Long teamId, String badgeImageUrl) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("teamNotFound"));

        team.changeBadgeImageUrl(badgeImageUrl);

        team.setAiBadgeInProgress(false);

        teamRepository.save(team);
    }

    @Override
    public void resetAiBadgeProgress(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("teamNotFound"));

        team.setAiBadgeInProgress(false);
        teamRepository.save(team);
    }

    @Override
    public Long incrementGivedPumati(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("teamNotFound"));
        team.increaseGivedPumati();
        teamRepository.save(team);
        return team.getGivedPumatiCount();
    }

    @Override
    public Long incrementReceivedPumati(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("teamNotFound"));
        team.increaseReceivedPumati();
        teamRepository.save(team);
        return team.getReceivedPumatiCount();
    }

    @Override
    public void resetAllPumatiCounts() {
        List<Team> teams = teamRepository.findAll();
        for (Team team : teams) {
            team.resetPumatiCounts();
        }
    }

    @Override
    public Team dtoToEntity(TeamCreateRequestDTO dto) {
        return Team.builder()
                .term(dto.getTerm())
                .number(dto.getNumber())
                .build();
    }

    private Integer findTeamProjectRank(Long teamId) {
        if (!projectService.existsByTeamId(teamId)) {
            return null;
        }

        ProjectResponseDTO project = projectService.getByTeamId(teamId);
        Long projectId = project.getId();

        try {
            return projectRankingSnapshotService.getLatestSnapshot()
                    .getData().stream()
                    .filter(item -> projectId.equals(item.getProjectId()))
                    .findFirst()
                    .map(RankingItemDTO::getRank)
                    .orElse(null);
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    private Long findTeamProjectId(Long teamId) {
        if (!projectService.existsByTeamId(teamId)) {
            return null;
        }

        ProjectResponseDTO project = projectService.getByTeamId(teamId);
        return project.getId();
    }

}
