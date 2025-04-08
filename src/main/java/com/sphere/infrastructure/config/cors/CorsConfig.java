package com.sphere.infrastructure.config.cors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * 跨域配置
 * 配置Spring Cloud Gateway的跨域访问策略
 * 支持所有来源、方法和请求头的跨域请求
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class CorsConfig {

    /**
     * 跨域过滤器
     * 配置跨域访问策略，包括：
     * 1. 允许所有来源
     * 2. 允许所有HTTP方法
     * 3. 允许所有请求头
     * 4. 允许携带认证信息
     * 5. 预检请求缓存时间
     *
     * @return CorsWebFilter 跨域过滤器
     */
    @Bean
    public CorsWebFilter corsFilter() {
        log.debug("初始化跨域配置");
        
        // 创建CORS配置
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源
        config.addAllowedOriginPattern("*");
        // 允许所有HTTP方法
        config.addAllowedMethod("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许携带认证信息
        config.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        config.setMaxAge(600L);

        // 创建CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        // 注册CORS配置，应用到所有路径
        source.registerCorsConfiguration("/**", config);
        
        log.debug("跨域配置初始化完成");
        return new CorsWebFilter(source);
    }
}
