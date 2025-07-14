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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(6).toLocalDate().atStartOfDay();

        List<ProjectRankingSnapshot> allSnapshots = projectRankingSnapshotRepository
                .findAllByRequestedAtBetween(start, now);

        Map<LocalDate, ProjectRankingSnapshot> latestPerDate = new HashMap<>();
        allSnapshots.stream()
                .sorted(Comparator.comparing(ProjectRankingSnapshot::getRequestedAt).reversed())
                .forEach(snap -> {
                    LocalDate date = snap.getRequestedAt().toLocalDate();
                    latestPerDate.putIfAbsent(date, snap);
                });

        List<LocalDate> last7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            last7Days.add(now.toLocalDate().minusDays(i));
        }

        List<ProjectRankingSnapshotResponseDTO> filledList = new ArrayList<>();
        ProjectRankingSnapshot lastKnownSnapshot = null;

        for (int i = 0; i < last7Days.size(); i++) {
            LocalDate date = last7Days.get(i);

            if (latestPerDate.containsKey(date)) {
                lastKnownSnapshot = latestPerDate.get(date);
            } else if (i == 0 && lastKnownSnapshot == null) {
                try {
                    lastKnownSnapshot = projectRankingSnapshotRepository
                            .findTopByOrderByRequestedAtDesc()
                            .orElse(null);
                    log.warn("No snapshot found for the first day. Using latest snapshot as fallback: {}",
                            lastKnownSnapshot != null ? lastKnownSnapshot.getRequestedAt() : "null");
                } catch (Exception e) {
                    log.error("Failed to fetch the latest snapshot for fallback", e);
                }
            }

            if (lastKnownSnapshot != null) {
                filledList.add(entityToDTO(lastKnownSnapshot));
            } else {
                filledList.add(null);
            }
        }

        return filledList;
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
