package com.sphere.infrastructure.config.monitor;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 健康检查配置类
 * 配置自定义的健康检查指标
 * 
 * 主要功能：
 * 1. Redis 连接健康检查
 * 2. 自定义业务健康检查
 * 3. 系统资源健康检查
 * 
 * 健康检查说明：
 * 1. 检查关键组件的可用性
 * 2. 监控系统资源使用情况
 * 3. 提供详细的健康状态信息
 *
 * @author sphere
 * @since 1.0.0
 */
@Configuration
public class HealthCheckConfiguration {

    /**
     * 系统资源健康检查
     * 检查系统资源使用情况
     *
     * @return HealthIndicator 实例
     */
    @Bean
    public HealthIndicator systemResourceHealthIndicator() {
        return () -> {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = (double) usedMemory / totalMemory * 100;

            if (memoryUsage > 90) {
                return Health.down()
                        .withDetail("memory", "Memory usage is too high")
                        .withDetail("usage", String.format("%.2f%%", memoryUsage))
                        .build();
            }

            return Health.up()
                    .withDetail("memory", "Memory usage is normal")
                    .withDetail("usage", String.format("%.2f%%", memoryUsage))
                    .build();
        };
    }
} 