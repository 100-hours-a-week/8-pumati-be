package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Team;
import com.tebutebu.apiserver.dto.project.response.ProjectResponseDTO;
import com.tebutebu.apiserver.dto.snapshot.response.RankingItemDTO;
import com.tebutebu.apiserver.dto.team.request.TeamCreateRequestDTO;
import com.tebutebu.apiserver.dto.team.response.TeamListResponseDTO;
import com.tebutebu.apiserver.dto.team.response.TeamResponseDTO;
import com.tebutebu.apiserver.repository.TeamRepository;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

    private final TeamRepository teamRepository;

    private final ProjectService projectService;

    private final ProjectRankingSnapshotService projectRankingSnapshotService;

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

        Team team = teamRepository.save(dtoToEntity(dto));
        return team.getId();
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
