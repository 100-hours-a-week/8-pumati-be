package com.tebutebu.apiserver.service.token.redis;

import com.tebutebu.apiserver.infrastructure.redis.util.RefreshTokenRedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
@RequiredArgsConstructor
public class RefreshTokenRedisServiceImpl implements RefreshTokenRedisService {

    private final RedisTemplate<String, Long> redisTemplate;

    private final RefreshTokenRedisKeyUtil keyUtil;

    @Override
    public void save(String token, Long memberId, long ttlMinutes) {
        String key = keyUtil.toRedisKey(token);
        redisTemplate.opsForValue().set(key, memberId, Duration.ofMinutes(ttlMinutes));
        log.debug("Saved refresh token to Redis with key={} and TTL={}m", key, ttlMinutes);
    }

    @Override
    public Long getMemberId(String token) {
        String key = keyUtil.toRedisKey(token);
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String token) {
        String key = keyUtil.toRedisKey(token);
        redisTemplate.delete(key);
        log.debug("Deleted refresh token from Redis with key={}", key);
    }

    @Override
    public boolean exists(String token) {
        String key = keyUtil.toRedisKey(token);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

}
