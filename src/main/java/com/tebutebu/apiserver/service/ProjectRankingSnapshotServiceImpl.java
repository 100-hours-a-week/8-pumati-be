package com.tebutebu.apiserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.ProjectRankingSnapshot;
import com.tebutebu.apiserver.repository.ProjectRankingSnapshotRepository;
import com.tebutebu.apiserver.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                    "gived_pumati_count", p.getTeam().getGivedPumatiCount()
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
