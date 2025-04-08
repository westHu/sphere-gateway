package com.sphere.infrastructure.cache;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisService {

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(getRedisFullKey(key));
    }

    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(getRedisFullKey(key), value);
        } catch (Exception ignored) {
        }
    }

    public void set(String key, Object value, long time) {
        try {
            String fullKey = getRedisFullKey(key);
            if (time > 0) {
                redisTemplate.opsForValue().set(fullKey, value, time, TimeUnit.SECONDS);
            } else {
                set(fullKey, value);
            }
        } catch (Exception ignored) {
        }
    }

    private String getRedisFullKey(String key) {
        String redis_prefix = "SPHERE_";

        if (key.startsWith(redis_prefix)) {
            return key;
        }
        return redis_prefix + key;
    }

}