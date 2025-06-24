package com.tebutebu.apiserver.infrastructure.redis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.data.redis.prefix")
public class RedisPrefixProperties {
    private String refreshToken;
}
