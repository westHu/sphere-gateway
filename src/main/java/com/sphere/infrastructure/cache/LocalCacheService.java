package com.sphere.infrastructure.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

/**
 * 本地缓存服务类
 * 提供本地缓存的操作方法
 * 
 * 主要功能：
 * 1. 缓存存取
 * 2. 缓存删除
 * 3. 缓存统计
 * 
 * 使用说明：
 * 1. 使用 @Cacheable 注解进行方法缓存
 * 2. 使用 @CacheEvict 注解清除缓存
 * 3. 使用 @CachePut 注解更新缓存
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@Service
public class LocalCacheService {

    private final CacheManager cacheManager;

    public LocalCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * 获取缓存值
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @return 缓存值
     */
    public Object get(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            return wrapper != null ? wrapper.get() : null;
        }
        return null;
    }

    /**
     * 获取缓存值，如果不存在则通过回调函数获取
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param callable 回调函数
     * @return 缓存值
     */
    public Object get(String cacheName, String key, Callable<Object> callable) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            try {
                return cache.get(key, callable);
            } catch (Exception e) {
                log.error("Failed to get cache value for key: {}", key, e);
                return null;
            }
        }
        return null;
    }

    /**
     * 设置缓存值
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param value 缓存值
     */
    public void put(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    /**
     * 删除缓存
     *
     * @param cacheName 缓存名称
     * @param key 缓存键
     */
    public void evict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    /**
     * 清空缓存
     *
     * @param cacheName 缓存名称
     */
    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
} 