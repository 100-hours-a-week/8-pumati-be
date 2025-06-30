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

    private final RedisTemplate<String, ProjectRankingSnapshotResponseDTO> snapshotRedisTemplate;

    private final RedisTemplate<String, String> stringRedisTemplate;

    private final RedisTemplate<String, Boolean> booleanRedisTemplate;

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
        boolean success = false;
        LocalDateTime now = LocalDateTime.now();

        try {
            long lockWaitTimeSeconds = 15, lockLeaseTimeSeconds = 60;
            isLocked = lock.tryLock(lockWaitTimeSeconds, lockLeaseTimeSeconds, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("Failed to acquire lock for snapshot registration.");
                throw new BusinessException(BusinessErrorCode.SNAPSHOT_LOCK_UNAVAILABLE);
            }

            log.info("Lock acquired for snapshot registration.");

            String latestCacheKey = snapshotCacheKeyPrefix + snapshotCacheKeyLatestSuffix;
            String cachedSnapshotId = stringRedisTemplate.opsForValue().get(latestCacheKey);
            if (cachedSnapshotId != null) {
                Long snapshotId = Long.parseLong(cachedSnapshotId);
                log.info("Reusing cached snapshot with ID={}", snapshotId);
                success = true;
                return snapshotId;
            }

            // 캐시에는 없지만 DB에 스냅샷이 있는 경우 fallback 확인
            LocalDateTime threshold = now.minusMinutes(snapshotDurationMinutes);
            ProjectRankingSnapshot latestSnapshot = projectRankingSnapshotRepository
                    .findTopByOrderByRequestedAtDesc()
                    .orElse(null);

            if (latestSnapshot != null
                    && !projectRepository.existsByCreatedAtAfter(latestSnapshot.getRequestedAt())
                    && latestSnapshot.getRequestedAt().isAfter(threshold)) {

                long remainingTime = Duration.between(now,latestSnapshot.getRequestedAt().plusMinutes(snapshotDurationMinutes)).toMinutes();
                if (remainingTime > 0) {
                    stringRedisTemplate.opsForValue().set(
                            latestCacheKey,
                            latestSnapshot.getId().toString(),
                            Duration.ofMinutes(remainingTime)
                    );
                }

                log.info("Reusing DB fallback snapshot with ID={}", latestSnapshot.getId());
                success = true;
                return latestSnapshot.getId();
            }

            // 중복 생성 방지 플래그 확인
            Boolean isGenerating = booleanRedisTemplate.opsForValue()
                    .setIfAbsent(snapshotGeneratingKey, true, Duration.ofSeconds(snapshotGeneratingTtlSeconds));
            if (Boolean.FALSE.equals(isGenerating)) {
                log.warn("Snapshot is already being generated. Skipping duplicate request.");
                throw new BusinessException(BusinessErrorCode.SNAPSHOT_ALREADY_IN_PROGRESS);
            }

            // 새로 생성
            Long createdId = createAndSaveSnapshot();
            success = true; // 생성 성공 시에만 success = true
            return createdId;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("lockInterrupted", e);
        } finally {
            // 스냅샷 생성이 성공한 경우에만 키 삭제
            if (success) {
                booleanRedisTemplate.delete(snapshotGeneratingKey);
            }
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
        ProjectRankingSnapshotResponseDTO cachedSnapshot = snapshotRedisTemplate.opsForValue().get(cacheKey);

        if (cachedSnapshot != null) {
            return cachedSnapshot;
        }

        ProjectRankingSnapshotResponseDTO projectRankingSnapshotResponseDTO = entityToDTO(snapshot);
        snapshotRedisTemplate.opsForValue().set(cacheKey, projectRankingSnapshotResponseDTO, Duration.ofMinutes(snapshotDurationMinutes));
        return projectRankingSnapshotResponseDTO;
    }

    private Long createAndSaveSnapshot() {
        List<RankingItemDTO> ranking = generateRanking();
        String json = serializeToJson(ranking);
        ProjectRankingSnapshot saved = persistSnapshot(json);
        cacheSnapshot(saved, ranking);
        return saved.getId();
    }

    private List<RankingItemDTO> generateRanking() {
        List<Project> projects = projectRepository.findAllForRanking();

        List<RankingItemDTO> rankingList = new ArrayList<>();
        int rank = 1;
        for (Project p : projects) {
            if (p.getId() == null || p.getTeam() == null || p.getTeam().getGivedPumatiCount() == null) continue;

            rankingList.add(RankingItemDTO.builder()
                    .projectId(p.getId())
                    .rank(rank++)
                    .givedPumatiCount(p.getTeam().getGivedPumatiCount())
                    .build());
        }

        return rankingList;
    }

    private String serializeToJson(List<RankingItemDTO> ranking) {
        try {
            return objectMapper.writeValueAsString(Map.of("projects", ranking));
        } catch (JsonProcessingException e) {
            throw new BusinessException(BusinessErrorCode.SNAPSHOT_SERIALIZATION_FAILED, e);
        }
    }

    private ProjectRankingSnapshot persistSnapshot(String json) {
        ProjectRankingSnapshot newSnap = ProjectRankingSnapshot.builder()
                .rankingData(json)
                .requestedAt(LocalDateTime.now())
                .build();

        ProjectRankingSnapshot saved = projectRankingSnapshotRepository.save(newSnap);
        projectRankingSnapshotRepository.flush();
        return saved;
    }

    private void cacheSnapshot(ProjectRankingSnapshot snapshot, List<RankingItemDTO> ranking) {
        ProjectRankingSnapshotResponseDTO dto = ProjectRankingSnapshotResponseDTO.builder()
                .id(snapshot.getId())
                .data(ranking)
                .build();

        String idKey = snapshotCacheKeyPrefix + snapshot.getId();
        String latestKey = snapshotCacheKeyPrefix + snapshotCacheKeyLatestSuffix;

        snapshotRedisTemplate.opsForValue().set(idKey, dto, Duration.ofMinutes(snapshotDurationMinutes));
        stringRedisTemplate.opsForValue().set(latestKey, snapshot.getId().toString(), Duration.ofMinutes(snapshotDurationMinutes));

        log.info("New snapshot created with ID={}", snapshot.getId());
    }

}
