package com.sphere.infrastructure.cache;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务
 * 提供Redis缓存的常用操作，包括：
 * 1. 获取缓存值
 * 2. 设置缓存值（支持过期时间）
 * 3. 自动添加前缀
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@Component
public class RedisService {

    /**
     * Redis操作模板
     */
    @Resource
    RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis键前缀
     */
    private static final String REDIS_PREFIX = "SPHERE_";

    /**
     * 获取缓存值
     * 根据键获取对应的缓存值，如果键为空则返回null
     *
     * @param key 缓存键
     * @return 缓存值，如果不存在则返回null
     */
    public Object get(String key) {
        if (StringUtils.isBlank(key)) {
            log.warn("获取缓存值失败：键为空");
            return null;
        }
        try {
            String fullKey = getRedisFullKey(key);
            Object value = redisTemplate.opsForValue().get(fullKey);
            log.debug("获取缓存值成功 - 键: {}, 值: {}", fullKey, value);
            return value;
        } catch (Exception e) {
            log.error("获取缓存值异常 - 键: {}, 错误: {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置缓存值
     * 将值存入缓存，不设置过期时间
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        if (StringUtils.isBlank(key)) {
            log.warn("设置缓存值失败：键为空");
            return;
        }
        try {
            String fullKey = getRedisFullKey(key);
            redisTemplate.opsForValue().set(fullKey, value);
            log.debug("设置缓存值成功 - 键: {}, 值: {}", fullKey, value);
        } catch (Exception e) {
            log.error("设置缓存值异常 - 键: {}, 值: {}, 错误: {}", key, value, e.getMessage(), e);
        }
    }

    /**
     * 设置缓存值（带过期时间）
     * 将值存入缓存，并设置过期时间
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param time 过期时间（秒）
     */
    public void set(String key, Object value, long time) {
        if (StringUtils.isBlank(key)) {
            log.warn("设置缓存值失败：键为空");
            return;
        }
        try {
            String fullKey = getRedisFullKey(key);
            if (time > 0) {
                redisTemplate.opsForValue().set(fullKey, value, time, TimeUnit.SECONDS);
                log.debug("设置缓存值成功（带过期时间） - 键: {}, 值: {}, 过期时间: {}秒", fullKey, value, time);
            } else {
                set(fullKey, value);
            }
        } catch (Exception e) {
            log.error("设置缓存值异常 - 键: {}, 值: {}, 过期时间: {}秒, 错误: {}", 
                    key, value, time, e.getMessage(), e);
        }
    }

    /**
     * 获取完整的Redis键
     * 自动添加前缀，如果已经包含前缀则直接返回
     *
     * @param key 原始键
     * @return 完整的Redis键
     */
    private String getRedisFullKey(String key) {
        if (key.startsWith(REDIS_PREFIX)) {
            return key;
        }
        return REDIS_PREFIX + key;
    }
}