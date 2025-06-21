package com.tebutebu.apiserver.integration.redis;

import com.tebutebu.apiserver.fixture.redis.RedisKeyValueFixture;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Redis 캐시 통합 테스트")
class RedisCacheIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Nested
    @DisplayName("기본 RedisTemplate 동작 검증")
    class RedisTemplateBasic {

        @Test
        @DisplayName("Redis 데이터 저장 후 조회 성공")
        void redisSetAndGetSuccess() {
            // given
            String key = RedisKeyValueFixture.key("basic");
            String value = RedisKeyValueFixture.sampleValue();

            // when
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(30));
            Object cached = redisTemplate.opsForValue().get(key);

            // then
            assertThat(cached).isEqualTo(value);
        }

        @Test
        @DisplayName("존재하지 않는 키 조회 시 null 반환")
        void getNonExistentKeyReturnsNull() {
            // given
            String missingKey = RedisKeyValueFixture.key("nonexistent");

            // when
            Object result = redisTemplate.opsForValue().get(missingKey);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("TTL 관련 동작")
    class TtlOperation {

        @Test
        @DisplayName("TTL이 만료되면 조회되지 않음")
        void redisTTLExpires() throws InterruptedException {
            // given
            String key = RedisKeyValueFixture.key("ttl");
            String value = RedisKeyValueFixture.expiringValue();
            redisTemplate.opsForValue().set(key, value, Duration.ofMillis(600));

            // when
            Thread.sleep(700); // TTL보다 살짝 기다림
            Object result = redisTemplate.opsForValue().get(key);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("덮어쓰면서 TTL 갱신")
        void overwriteValueAlsoUpdatesTTL() throws InterruptedException {
            // given
            String key = RedisKeyValueFixture.key("ttlOverwrite");
            String original = "original";
            String updated = "updated";
            redisTemplate.opsForValue().set(key, original, Duration.ofMillis(1000));
            Thread.sleep(600);

            // when
            redisTemplate.opsForValue().set(key, updated, Duration.ofMillis(1000));
            Thread.sleep(600);
            Object result = redisTemplate.opsForValue().get(key);

            // then
            assertThat(result).isEqualTo(updated);
        }
    }

    @Nested
    @DisplayName("덮어쓰기 동작")
    class OverwriteOperation {

        @Test
        @DisplayName("같은 키로 값을 덮어쓰면 이전 값은 대체됨")
        void overwriteValueWithSameKey() {
            // given
            String key = RedisKeyValueFixture.key("overwrite");
            String original = "originalValue";
            String updated = "updatedValue";
            redisTemplate.opsForValue().set(key, original, Duration.ofSeconds(60));

            // when
            redisTemplate.opsForValue().set(key, updated, Duration.ofSeconds(60));
            Object result = redisTemplate.opsForValue().get(key);

            // then
            assertThat(result).isEqualTo(updated);
        }
    }
}
