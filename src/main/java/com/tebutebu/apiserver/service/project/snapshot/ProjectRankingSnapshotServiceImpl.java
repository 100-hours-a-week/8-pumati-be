package com.tebutebu.apiserver.service.project.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.domain.ProjectRankingSnapshot;
import com.tebutebu.apiserver.dto.project.snapshot.response.ProjectRankingSnapshotResponseDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.RankingItemDTO;
import com.tebutebu.apiserver.global.errorcode.BusinessErrorCode;
import com.tebutebu.apiserver.global.exception.BusinessException;
import com.tebutebu.apiserver.repository.ProjectRankingSnapshotRepository;
import com.tebutebu.apiserver.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProjectRankingSnapshotServiceImpl implements ProjectRankingSnapshotService {

    private final ProjectRankingSnapshotRepository projectRankingSnapshotRepository;

    private final ProjectRepository projectRepository;

    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    private final RedissonClient redissonClient;

    @Value("${ranking.snapshot.duration.minutes:5}")
    private long snapshotDurationMinutes;

    @Value("${ranking.snapshot.cache.key-prefix}")
    private String snapshotCacheKeyPrefix;

    @Value("${ranking.snapshot.cache.key-latest-suffix:latest:id}")
    private String snapshotCacheKeyLatestSuffix;

    @Value("${ranking.snapshot.lock.key-register}")
    private String registerLockKey;

    @Value("${ranking.snapshot.cache.key-generating-flag:snapshot:generating}")
    private String snapshotGeneratingKey;

    @Value("${ranking.snapshot.cache.generating-ttl-seconds:60}")
    private long snapshotGeneratingTtlSeconds;

    @Override
    public Long register() {
        RLock lock = redissonClient.getLock(registerLockKey);
        boolean isLocked = false;
        LocalDateTime now = LocalDateTime.now();

        try {
            // 락을 최대 15초까지 대기하며, 60초 동안 점유함
            isLocked = lock.tryLock(15, 60, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("Failed to acquire lock for snapshot registration.");
                throw new BusinessException(BusinessErrorCode.SNAPSHOT_LOCK_UNAVAILABLE);
            }

            log.info("Lock acquired for snapshot registration.");

            // 캐시에 저장된 스냅샷 ID가 있는 경우 재사용
            String latestCacheKey = snapshotCacheKeyPrefix + snapshotCacheKeyLatestSuffix;
            String cachedSnapshotId = (String) redisTemplate.opsForValue().get(latestCacheKey);
            if (cachedSnapshotId != null) {
                Long snapshotId = Long.parseLong(cachedSnapshotId);
                log.info("Reusing cached snapshot with ID={}", snapshotId);
                return snapshotId;
            }

            // 스냅샷 생성 중인 경우 중단
            Boolean generating = redisTemplate.opsForValue()
                    .setIfAbsent(snapshotGeneratingKey, "true", Duration.ofSeconds(snapshotGeneratingTtlSeconds));
            if (Boolean.FALSE.equals(generating)) {
                log.warn("Snapshot is already being generated. Skipping duplicate request.");
                throw new BusinessException(BusinessErrorCode.SNAPSHOT_ALREADY_IN_PROGRESS);
            }

            // DB fallback 확인
            LocalDateTime threshold = now.minusMinutes(snapshotDurationMinutes);
            ProjectRankingSnapshot latestSnapshot = projectRankingSnapshotRepository
                    .findTopByOrderByRequestedAtDesc()
                    .orElse(null);

            if (latestSnapshot != null) {
                boolean hasNewProject = projectRepository.existsByCreatedAtAfter(latestSnapshot.getRequestedAt());

                if (!hasNewProject && latestSnapshot.getRequestedAt().isAfter(threshold)) {
                    long remainingTime = Duration.between(now, latestSnapshot.getRequestedAt().plusMinutes(snapshotDurationMinutes)).toMinutes();
                    if (remainingTime > 0) {
                        redisTemplate.opsForValue().set(latestCacheKey,
                                latestSnapshot.getId().toString(), Duration.ofMinutes(remainingTime));
                    }

                    log.info("Reusing DB fallback snapshot with ID={}", latestSnapshot.getId());
                    return latestSnapshot.getId();
                }
            }

            return createAndSaveSnapshot();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("lockInterrupted", e);
        } finally {
            // 생성 중 플래그 제거
            redisTemplate.delete(snapshotGeneratingKey);
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released for snapshot registration.");
            }
        }
    }

    @Override
    public ProjectRankingSnapshotResponseDTO getLatestSnapshot() {
        ProjectRankingSnapshot snapshot = projectRankingSnapshotRepository
                .findTopByOrderByRequestedAtDesc()
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.SNAPSHOT_NOT_FOUND));

        String cacheKey = snapshotCacheKeyPrefix + snapshot.getId();
        ProjectRankingSnapshotResponseDTO cachedSnapshot = (ProjectRankingSnapshotResponseDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedSnapshot != null) {
            return cachedSnapshot;
        }

        ProjectRankingSnapshotResponseDTO projectRankingSnapshotResponseDTO = entityToDTO(snapshot);
        redisTemplate.opsForValue().set(cacheKey, projectRankingSnapshotResponseDTO, Duration.ofMinutes(snapshotDurationMinutes));
        return projectRankingSnapshotResponseDTO;
    }

    private Long createAndSaveSnapshot() {
        List<Project> projects = projectRepository.findAllForRanking();

        List<RankingItemDTO> rankingList = new ArrayList<>();
        int rank = 1;
        for (Project p : projects) {
            if (p.getId() == null || p.getTeam() == null || p.getTeam().getGivedPumatiCount() == null) {
                continue;
            }
            rankingList.add(RankingItemDTO.builder()
                    .projectId(p.getId())
                    .rank(rank++)
                    .givedPumatiCount(p.getTeam().getGivedPumatiCount())
                    .build());
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(Map.of("projects", rankingList));
        } catch (JsonProcessingException e) {
            throw new BusinessException(BusinessErrorCode.SNAPSHOT_SERIALIZATION_FAILED, e);
        }

        ProjectRankingSnapshot newSnap = ProjectRankingSnapshot.builder()
                .rankingData(json)
                .requestedAt(LocalDateTime.now())
                .build();

        ProjectRankingSnapshot saved = projectRankingSnapshotRepository.save(newSnap);
        projectRankingSnapshotRepository.flush();

        String cacheKey = snapshotCacheKeyPrefix + saved.getId();
        ProjectRankingSnapshotResponseDTO dto = entityToDTO(saved);
        redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofMinutes(snapshotDurationMinutes));

        String latestCacheKey = snapshotCacheKeyPrefix + snapshotCacheKeyLatestSuffix;
        redisTemplate.opsForValue().set(latestCacheKey, saved.getId().toString(), Duration.ofMinutes(snapshotDurationMinutes));

        log.info("New snapshot created with ID={}", saved.getId());
        return saved.getId();
    }

}
