package com.sphere.infrastructure.config.monitor;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控配置类
 * 配置 Spring Boot Actuator 和 Micrometer 监控
 * 
 * 主要功能：
 * 1. 应用健康检查
 * 2. 性能指标收集
 * 3. 自定义监控指标
 * 4. Prometheus 集成
 * 
 * 监控指标说明：
 * 1. JVM 指标：内存使用、GC 情况、线程状态
 * 2. 系统指标：CPU 使用率、负载情况
 * 3. 应用指标：请求统计、响应时间、错误率
 * 4. 自定义指标：业务相关的统计信息
 *
 * @author sphere
 * @since 1.0.0
 */
@Configuration
public class MonitorConfiguration {

    /**
     * 配置 MeterRegistry
     * 用于收集和导出监控指标
     *
     * @return PrometheusMeterRegistry 实例
     */
    @Bean
    public MeterRegistry meterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    /**
     * 配置应用标签
     * 为所有监控指标添加应用标识
     *
     * @return MeterRegistryCustomizer 实例
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", "sphere-gateway");
    }

    /**
     * 配置 @Timed 注解支持
     * 用于方法级别的执行时间统计
     *
     * @param registry MeterRegistry 实例
     * @return TimedAspect 实例
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
} 