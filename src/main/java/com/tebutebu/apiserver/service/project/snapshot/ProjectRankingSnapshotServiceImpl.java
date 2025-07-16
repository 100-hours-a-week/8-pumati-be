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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Comparator;
import java.util.HashMap;

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
        Long cachedId = getSnapshotIdFromCache();
        if (cachedId != null) {
            if (cachedId <= 0) {
                throw new BusinessException(BusinessErrorCode.INVALID_SNAPSHOT_ID);
            }
            return cachedId;
        }

        Long fallback = getFallbackSnapshotId();
        if (fallback != null) {
            return fallback;
        }

        RLock lock = redissonClient.getLock(registerLockKey);
        boolean isLocked = false;
        boolean success = false;

        try {
            isLocked = tryAcquireLock(lock);
            if (!isLocked) {
                throw new BusinessException(BusinessErrorCode.SNAPSHOT_LOCK_UNAVAILABLE);
            }

            Long secondCachedId = getSnapshotIdFromCache();
            if (secondCachedId != null) {
                return secondCachedId;
            }

            Long secondFallback = getFallbackSnapshotId();
            if (secondFallback != null) {
                return secondFallback;
            }

            ensureNotGenerating();

            Long createdId = createAndSaveSnapshot();
            success = true;
            return createdId;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("lockInterrupted", e);
        } finally {
            if (success) {
                try {
                    booleanRedisTemplate.delete(snapshotGeneratingKey);
                } catch (Exception e) {
                    log.warn("Failed to delete snapshotGeneratingKey", e);
                }
            }
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean tryAcquireLock(RLock lock) throws InterruptedException {
        long waitTime = 15, leaseTime = 60;
        boolean isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        if (isLocked) {
            log.info("Lock acquired for snapshot registration.");
        } else {
            log.warn("Failed to acquire lock for snapshot registration.");
        }
        return isLocked;
    }

    private Long getSnapshotIdFromCache() {
        String cacheKey = snapshotCacheKeyPrefix + snapshotCacheKeyLatestSuffix;
        String cachedId = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedId != null) {
            log.info("Reusing cached snapshot with ID={}", cachedId);
            return Long.parseLong(cachedId);
        }
        return null;
    }

    private Long getFallbackSnapshotId() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(snapshotDurationMinutes);

        ProjectRankingSnapshot snapshot = projectRankingSnapshotRepository
                .findTopByOrderByRequestedAtDesc()
                .orElse(null);

        if (snapshot == null) {
            return null;
        }

        boolean noNewProject = !projectRepository.existsByCreatedAtAfter(snapshot.getRequestedAt());
        boolean isValid = snapshot.getRequestedAt().isAfter(threshold);

        if (noNewProject && isValid) {
            long ttl = Duration.between(now, snapshot.getRequestedAt().plusMinutes(snapshotDurationMinutes)).toMinutes();
            if (ttl > 0) {
                String cacheKey = snapshotCacheKeyPrefix + snapshotCacheKeyLatestSuffix;
                stringRedisTemplate.opsForValue().set(cacheKey, snapshot.getId().toString(), Duration.ofMinutes(ttl));
            }
            log.info("Reusing DB fallback snapshot with ID={}", snapshot.getId());
            return snapshot.getId();
        }

        return null;
    }

    private void ensureNotGenerating() {
        Boolean isGenerating = booleanRedisTemplate.opsForValue()
                .setIfAbsent(snapshotGeneratingKey, true, Duration.ofSeconds(snapshotGeneratingTtlSeconds));
        if (Boolean.FALSE.equals(isGenerating)) {
            log.warn("Snapshot is already being generated. Skipping duplicate request.");
            throw new BusinessException(BusinessErrorCode.SNAPSHOT_ALREADY_IN_PROGRESS);
        }
    }

    @Override
    public ProjectRankingSnapshotResponseDTO getLatestSnapshot() {
        ProjectRankingSnapshot snapshot = projectRankingSnapshotRepository
                .findTopByOrderByRequestedAtDesc()
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.SNAPSHOT_NOT_FOUND));

        String cacheKey = snapshotCacheKeyPrefix + snapshot.getId();
        try {
            ProjectRankingSnapshotResponseDTO cached = snapshotRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.convertValue(cached, ProjectRankingSnapshotResponseDTO.class); // 안전하게 변환
            }
        } catch (Exception e) {
            log.warn("Failed to deserialize cached snapshot for ID={} from Redis. Fallback to DB.", snapshot.getId(), e);
        }

        ProjectRankingSnapshotResponseDTO projectRankingSnapshotResponseDTO = entityToDTO(snapshot);
        snapshotRedisTemplate.opsForValue().set(cacheKey, projectRankingSnapshotResponseDTO, Duration.ofMinutes(snapshotDurationMinutes));
        return projectRankingSnapshotResponseDTO;
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
            if (p.getId() == null || p.getTeam() == null || p.getTeam().getGivedPumatiCount() == null) {
                continue;
            }
            rankingList.add(RankingItemDTO.builder()
                    .projectId(p.getId())
                    .rank(rank++)
                    .givedPumatiCount(p.getTeam().getGivedPumatiCount())
                    .receivedPumatiCount(p.getTeam().getReceivedPumatiCount())
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
        try {
            snapshotRedisTemplate.opsForValue().set(idKey, dto, Duration.ofMinutes(snapshotDurationMinutes));
            stringRedisTemplate.opsForValue().set(latestKey, snapshot.getId().toString(), Duration.ofMinutes(snapshotDurationMinutes));
        } catch (Exception e) {
            log.error("Failed to cache snapshot ID={} into Redis", snapshot.getId(), e);
        }
    }

}
