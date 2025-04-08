package com.sphere.infrastructure.config.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 监控指标配置类
 * 配置自定义的监控指标
 * 
 * 主要功能：
 * 1. 请求统计指标
 * 2. 性能监控指标
 * 3. 业务统计指标
 * 
 * 监控指标说明：
 * 1. 请求计数器：统计请求总数
 * 2. 响应时间：统计请求处理时间
 * 3. 错误计数器：统计错误请求数
 * 4. 业务指标：统计业务相关的数据
 *
 * @author sphere
 * @since 1.0.0
 */
@Configuration
public class MetricsConfiguration {

    /**
     * 请求计数器
     * 统计总请求数
     *
     * @param registry MeterRegistry 实例
     * @return Counter 实例
     */
    @Bean
    public Counter requestCounter(MeterRegistry registry) {
        return Counter.builder("gateway.requests.total")
                .description("Total number of requests")
                .tag("type", "all")
                .register(registry);
    }

    /**
     * 错误请求计数器
     * 统计错误请求数
     *
     * @param registry MeterRegistry 实例
     * @return Counter 实例
     */
    @Bean
    public Counter errorCounter(MeterRegistry registry) {
        return Counter.builder("gateway.requests.errors")
                .description("Total number of error requests")
                .tag("type", "error")
                .register(registry);
    }

    /**
     * 请求处理时间统计
     * 统计请求处理耗时
     *
     * @param registry MeterRegistry 实例
     * @return Timer 实例
     */
    @Bean
    public Timer requestTimer(MeterRegistry registry) {
        return Timer.builder("gateway.requests.latency")
                .description("Request processing time")
                .tag("type", "latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    /**
     * 业务处理时间统计
     * 统计业务处理耗时
     *
     * @param registry MeterRegistry 实例
     * @return Timer 实例
     */
    @Bean
    public Timer businessTimer(MeterRegistry registry) {
        return Timer.builder("gateway.business.latency")
                .description("Business processing time")
                .tag("type", "business")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
} 