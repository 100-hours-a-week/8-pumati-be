package com.tebutebu.apiserver.integration.concurrency.project.snapshot;

import com.tebutebu.apiserver.domain.ProjectRankingSnapshot;
import com.tebutebu.apiserver.dto.project.snapshot.response.ProjectRankingSnapshotResponseDTO;
import com.tebutebu.apiserver.dto.project.snapshot.response.RankingItemDTO;
import com.tebutebu.apiserver.repository.ProjectRankingSnapshotRepository;
import com.tebutebu.apiserver.service.project.snapshot.ProjectRankingSnapshotService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ProjectRankingSnapshot 동시성 테스트")
public class ProjectRankingSnapshotConcurrencyTest {

    @Autowired
    private ProjectRankingSnapshotService snapshotService;

    @Autowired
    private ProjectRankingSnapshotRepository snapshotRepository;

    @Autowired
    private RedisTemplate<String, ProjectRankingSnapshotResponseDTO> snapshotRedisTemplate;

    @BeforeEach
    void clearSnapshots() {
        snapshotRepository.deleteAll();
    }

    @BeforeEach
    void clearCache() {
        Objects.requireNonNull(snapshotRedisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @Nested
    @DisplayName("register() 호출 시")
    class Register {

        @RepeatedTest(5)
        @DisplayName("동시 호출 시 register() 결과는 하나의 Snapshot ID만 생성되고 형식도 유효함")
        void concurrentRegister_createsSingleSnapshot() throws Exception {
            // given
            int threadCount = 50;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);
            List<Long> snapshotIds = new CopyOnWriteArrayList<>();
            List<Exception> exceptions = new CopyOnWriteArrayList<>();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            try {
                for (int i = 0; i < threadCount; i++) {
                    final int threadNum = i;
                    executor.submit(() -> {
                        try {
                            startLatch.await();
                            log.info("Thread {} starting register()", threadNum);
                            Long id = snapshotService.register();
                            snapshotIds.add(id);
                            log.info("Thread {} completed with ID={}", threadNum, id);
                        } catch (Exception e) {
                            log.error("Thread {} failed with error: {}", threadNum, e.getMessage());
                            exceptions.add(e);
                        } finally {
                            endLatch.countDown();
                        }
                    });
                }

                startLatch.countDown();
                endLatch.await(30, TimeUnit.SECONDS);

                // then
                List<Long> distinctIds = snapshotIds.stream().distinct().toList();
                Long expectedId = distinctIds.getFirst();

                assertThat(exceptions).isEmpty();
                assertThat(snapshotIds).hasSize(threadCount);
                assertThat(snapshotIds).allMatch(Objects::nonNull);
                assertThat(snapshotIds).allMatch(id -> id.equals(expectedId));
                assertThat(distinctIds).hasSize(1);
                assertThat(snapshotRepository.count()).isEqualTo(1);

                // entityToDTO()를 통해 스냅샷 내용 검증
                ProjectRankingSnapshot snapshot = snapshotRepository.findById(expectedId)
                        .orElseThrow(() -> new AssertionError("Snapshot not found in DB"));

                ProjectRankingSnapshotResponseDTO dto = snapshotService.entityToDTO(snapshot);

                assertThat(dto).isNotNull();
                assertThat(dto.getId()).isEqualTo(expectedId);
                assertThat(dto.getData()).isNotNull();

                if (!dto.getData().isEmpty()) {
                    assertThat(dto.getData().getFirst().getRank()).isEqualTo(1);
                    assertThat(dto.getData()).isSortedAccordingTo(
                            Comparator.comparingInt(RankingItemDTO::getRank)
                    );
                }

            } finally {
                executor.shutdownNow();
            }
        }

        @Test
        @DisplayName("연속 호출 시 캐시된 결과 재사용")
        void consecutiveRegister_reusesCachedResult() throws InterruptedException {
            // given
            Long firstId = snapshotService.register();

            // when - 짧은 간격으로 연속 호출
            List<Long> subsequentIds = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                subsequentIds.add(snapshotService.register());
                Thread.sleep(100);
            }

            // then
            assertThat(subsequentIds).allMatch(id -> id.equals(firstId));
            assertThat(snapshotRepository.count()).isEqualTo(1);
        }

    }

    @Nested
    @DisplayName("getLatestSnapshot() 호출 시")
    class GetLatestSnapshot {

        @Test
        @DisplayName("ID만 캐시되어 있는 경우에도 본문이 DB에서 조회되어 캐싱된다")
        void getLatestSnapshot_fetchesFromDBIfOnlyIdCached() {
            // given
            Long snapshotId = snapshotService.register();
            String bodyKey = "ranking:snapshot:" + snapshotId; // 환경 변수 값과 일치해야 함
            snapshotRedisTemplate.delete(bodyKey);

            // when
            ProjectRankingSnapshotResponseDTO snapshot = snapshotService.getLatestSnapshot();

            // then
            assertThat(snapshot).isNotNull();
            assertThat(snapshot.getId()).isEqualTo(snapshotId);
        }

        @Test
        @DisplayName("캐시에 DTO가 존재하는 경우, DB 조회 없이 캐시된 DTO 반환")
        void getLatestSnapshot_returnsCachedDTO() {
            // given
            Long snapshotId = snapshotService.register();

            // when
            ProjectRankingSnapshotResponseDTO dto1 = snapshotService.getLatestSnapshot();
            ProjectRankingSnapshotResponseDTO dto2 = snapshotService.getLatestSnapshot();

            // then
            assertThat(dto1.getId()).isEqualTo(snapshotId);
            assertThat(dto2.getId()).isEqualTo(snapshotId);
        }
    }
}
