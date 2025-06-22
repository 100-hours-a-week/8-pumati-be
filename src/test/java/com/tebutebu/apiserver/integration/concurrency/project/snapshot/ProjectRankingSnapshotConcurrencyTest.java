package com.tebutebu.apiserver.integration.concurrency.project.snapshot;

import com.tebutebu.apiserver.repository.ProjectRankingSnapshotRepository;
import com.tebutebu.apiserver.service.project.snapshot.ProjectRankingSnapshotService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void clearSnapshots() {
        snapshotRepository.deleteAll();
        // Clear Redis cache as well
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @Nested
    @DisplayName("register() 호출 시")
    class Register {

        @RepeatedTest(5)
        @DisplayName("동시 호출 시 register() 결과는 하나의 Snapshot ID만 생성됨")
        void concurrentRegister_createsSingleSnapshot() throws InterruptedException {
            // given
            int threadCount = 50;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);
            List<Long> snapshotIds = new CopyOnWriteArrayList<>();
            List<Exception> exceptions = new CopyOnWriteArrayList<>();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // when
            for (int i = 0; i < threadCount; i++) {
                final int threadNum = i;
                executor.submit(() -> {
                    try {
                        startLatch.await(); // All threads start at the same time
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

            startLatch.countDown(); // Start all threads
            endLatch.await(30, TimeUnit.SECONDS); // Wait up to 30 seconds

            // then
            List<Long> distinctIds = snapshotIds.stream().distinct().toList();

            log.info("All snapshot IDs: {}", snapshotIds);
            log.info("Distinct snapshot IDs: {}", distinctIds);
            log.info("Snapshot count in DB: {}", snapshotRepository.count());

            if (!exceptions.isEmpty()) {
                log.error("Exceptions occurred: {}", exceptions);
            }

            assertThat(exceptions).isEmpty();
            assertThat(snapshotIds).hasSize(threadCount);
            assertThat(distinctIds).hasSize(1);
            assertThat(snapshotRepository.count()).isEqualTo(1);
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
}
