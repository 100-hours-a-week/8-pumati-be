package com.tebutebu.apiserver.fixture.redis;

public class RedisKeyValueFixture {

    public static String key(String name) {
        return "test:redis:" + name;
    }

    public static String sampleValue() {
        return "redis-value";
    }

    public static String expiringValue() {
        return "will-expire";
    }

}
