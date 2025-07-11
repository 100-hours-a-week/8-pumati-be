package com.tebutebu.apiserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tebutebu.apiserver.dto.project.snapshot.response.ProjectRankingSnapshotResponseDTO;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig singleServer = config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort);

        if (StringUtils.hasText(redisPassword)) {
            singleServer.setPassword(redisPassword);
        }

        return Redisson.create(config);
    }

    @Bean
    public RedisTemplate<String, ProjectRankingSnapshotResponseDTO> snapshotRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, ProjectRankingSnapshotResponseDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Long> customLongRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Long.class));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, String> customStringRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Boolean> customBooleanRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Boolean> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Boolean.class));
        template.afterPropertiesSet();
        return template;
    }

}
