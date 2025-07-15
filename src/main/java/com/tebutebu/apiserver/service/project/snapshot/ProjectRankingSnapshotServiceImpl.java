package com.tebutebu.apiserver.service.project.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.ProjectRankingSnapshot;
import com.tebutebu.apiserver.dto.project.snapshot.response.ProjectRankingSnapshotResponseDTO;
import com.tebutebu.apiserver.repository.ProjectRankingSnapshotRepository;
import com.tebutebu.apiserver.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.HashMap;
import java.util.NoSuchElementException;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProjectRankingSnapshotServiceImpl implements ProjectRankingSnapshotService {

    private final ProjectRankingSnapshotRepository projectRankingSnapshotRepository;

    private final ProjectRepository projectRepository;

    private final ObjectMapper objectMapper;

    @Value("${ranking.snapshot.duration.minutes:5}")
    private long snapshotDurationMinutes;

    @Override
    public Long register() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(snapshotDurationMinutes);
        return projectRankingSnapshotRepository
                .findTopByRequestedAtAfterOrderByRequestedAtDesc(threshold)
                .map(existingSnapshot -> {
                    boolean hasNewProject = projectRepository.existsByCreatedAtAfter(existingSnapshot.getRequestedAt());
                    if (!hasNewProject) {
                        return existingSnapshot.getId();
                    }
                    return createAndSaveSnapshot();
                })
                .orElseGet(this::createAndSaveSnapshot);
    }

    @Override
    public ProjectRankingSnapshotResponseDTO getLatestSnapshot() {
        ProjectRankingSnapshot snapshot = projectRankingSnapshotRepository
                .findTopByOrderByRequestedAtDesc()
                .orElseThrow(() -> new NoSuchElementException("snapshotNotFound"));
        return entityToDTO(snapshot);
    }

    @Override
    public List<ProjectRankingSnapshotResponseDTO> getSnapshotsForLast7Days() {
        LocalDate startDate = LocalDate.now()
                .with(java.time.DayOfWeek.MONDAY)
                .minusWeeks(1); // 지난주 월요일

        LocalDate endDate = startDate.plusDays(6); // 지난주 일요일

        List<ProjectRankingSnapshot> allSnapshots = projectRankingSnapshotRepository
                .findAllByRequestedAtBetween(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        Map<LocalDate, ProjectRankingSnapshot> latestPerDate = new HashMap<>();
        allSnapshots.stream()
                .sorted(Comparator.comparing(ProjectRankingSnapshot::getRequestedAt).reversed())
                .forEach(snapshot -> {
                    LocalDate date = snapshot.getRequestedAt().toLocalDate();
                    latestPerDate.putIfAbsent(date, snapshot);
                });

        List<ProjectRankingSnapshotResponseDTO> result = new ArrayList<>();
        ProjectRankingSnapshot lastKnownSnapshot = null;

        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            ProjectRankingSnapshot snapshot = latestPerDate.get(date);

            if (snapshot == null && i == 0) {
                snapshot = projectRankingSnapshotRepository
                        .findTopByRequestedAtBeforeOrderByRequestedAtDesc(date.atStartOfDay())
                        .orElse(null);

                if (snapshot != null) {
                    log.warn("No snapshot for first day ({}), fallback to closest previous: {}",
                            date, snapshot.getRequestedAt());
                }
            }

            if (snapshot == null) {
                snapshot = lastKnownSnapshot;
            }

            lastKnownSnapshot = snapshot;
            result.add(snapshot != null ? entityToDTO(snapshot) : null);
        }

        return result;
    }

    private Long createAndSaveSnapshot() {
        List<Project> projects = projectRepository.findAllForRanking();

        List<Map<String, Object>> rankingList = new ArrayList<>();
        int rank = 1;
        for (Project p : projects) {
            if (p.getId() == null || p.getTeam() == null || p.getTeam().getGivedPumatiCount() == null) {
                continue;
            }
            rankingList.add(Map.of(
                    "project_id", p.getId(),
                    "rank",           rank++,
                    "gived_pumati_count", p.getTeam().getGivedPumatiCount(),
                    "received_pumati_count", p.getTeam().getReceivedPumatiCount()
            ));
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(Map.of("projects", rankingList));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }

        ProjectRankingSnapshot newSnap = ProjectRankingSnapshot.builder()
                .rankingData(json)
                .requestedAt(LocalDateTime.now())
                .build();

        return projectRankingSnapshotRepository.save(newSnap).getId();
    }

}
