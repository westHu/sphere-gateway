package com.sphere.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存配置类
 * 使用 Caffeine 实现高性能本地缓存
 * 
 * 主要功能：
 * 1. 本地缓存配置
 * 2. 缓存策略设置
 * 3. 性能优化
 * 
 * 配置说明：
 * 1. 最大容量：10000 条
 * 2. 过期时间：1 小时
 * 3. 写入后过期
 * 4. 统计信息收集
 *
 * @author sphere
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class LocalCacheConfig {

    /**
     * 配置缓存管理器
     * 设置缓存策略和参数
     *
     * @return 配置好的缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000)                 // 最大缓存条数
                .expireAfterWrite(1, TimeUnit.HOURS) // 写入1小时后过期
                .recordStats());                    // 开启统计
        return cacheManager;
    }
} 