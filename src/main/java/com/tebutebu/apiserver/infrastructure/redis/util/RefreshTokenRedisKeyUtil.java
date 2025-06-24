package com.tebutebu.apiserver.infrastructure.redis.util;

import com.tebutebu.apiserver.infrastructure.redis.config.RedisPrefixProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RefreshTokenRedisKeyUtil {

    private final RedisPrefixProperties redisPrefixProperties;

    public String toRedisKey(String token) {
        return redisPrefixProperties.getRefreshToken() + ":" + token;
    }

}
