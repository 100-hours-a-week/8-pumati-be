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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProjectRankingSnapshotServiceImpl implements ProjectRankingSnapshotService {

    private final ProjectRankingSnapshotRepository projectRankingSnapshotRepository;

    private final ProjectRepository projectRepository;

    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${ranking.snapshot.duration.minutes:5}")
    private long snapshotDurationMinutes;

    @Value("${ranking.snapshot.cache.key-prefix}")
    private String snapshotCacheKeyPrefix;

    @Value("${ranking.snapshot.cache.latest-created-at-key}")
    private String projectLatestCreatedAtKey;

    @Override
    public Long register() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(snapshotDurationMinutes);

        String redisTimeStr = (String) redisTemplate.opsForValue().get(projectLatestCreatedAtKey);
        LocalDateTime latestProjectCreatedAt;
        if (redisTimeStr != null) {
            latestProjectCreatedAt = LocalDateTime.parse(redisTimeStr);
        } else {
            latestProjectCreatedAt = projectRepository.findLatestCreatedAt().orElse(LocalDateTime.MIN);
            redisTemplate.opsForValue().set(projectLatestCreatedAtKey, latestProjectCreatedAt.toString());
        }

        boolean hasNewProject = latestProjectCreatedAt.isAfter(threshold);

        return projectRankingSnapshotRepository
                .findTopByRequestedAtAfterOrderByRequestedAtDesc(threshold)
                .map(existingSnapshot -> hasNewProject
                        ? createAndSaveSnapshot()
                        : existingSnapshot.getId())
                .orElseGet(this::createAndSaveSnapshot);
    }

    @Override
    public ProjectRankingSnapshotResponseDTO getLatestSnapshot() {
        ProjectRankingSnapshot snapshot = projectRankingSnapshotRepository
                .findTopByOrderByRequestedAtDesc()
                .orElseThrow(() -> new NoSuchElementException("snapshotNotFound"));

        String cacheKey = snapshotCacheKeyPrefix + snapshot.getId();

        ProjectRankingSnapshotResponseDTO cached = (ProjectRankingSnapshotResponseDTO) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return cached;
        }

        ProjectRankingSnapshotResponseDTO dto = entityToDTO(snapshot);
        redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofMinutes(snapshotDurationMinutes));
        return dto;
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
                    "rank", rank++,
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

        ProjectRankingSnapshot saved = projectRankingSnapshotRepository.save(newSnap);

        String cacheKey = snapshotCacheKeyPrefix + saved.getId();
        ProjectRankingSnapshotResponseDTO dto = entityToDTO(saved);
        redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofMinutes(snapshotDurationMinutes));

        return saved.getId();
    }

}
