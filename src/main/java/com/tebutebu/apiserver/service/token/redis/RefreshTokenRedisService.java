package com.tebutebu.apiserver.service.token.redis;

public interface RefreshTokenRedisService {

    void save(String token, Long memberId, long ttlMinutes);

    Long getMemberId(String token);

    void delete(String token);

    boolean exists(String token);

}
