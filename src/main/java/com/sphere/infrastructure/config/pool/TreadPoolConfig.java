package com.sphere.infrastructure.config.pool;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * 配置网关使用的线程池，用于处理异步任务，如：
 * 1. 日志记录
 * 2. 监控告警
 * 3. 其他异步操作
 * 
 * 线程池参数可通过配置文件调整：
 * - thread.pool.corePoolSize: 核心线程数
 * - thread.pool.maxPoolSize: 最大线程数
 * - thread.pool.queueCapacity: 队列容量
 * - thread.pool.keepAliveSeconds: 线程存活时间
 *
 * @author sphere
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class TreadPoolConfig {

    /**
     * 核心线程数
     * 默认值：64
     */
    @Value("${thread.pool.corePoolSize:64}")
    int corePoolSize;

    /**
     * 最大线程数
     * 默认值：64
     */
    @Value("${thread.pool.maxPoolSize:64}")
    int maxPoolSize;

    /**
     * 队列容量
     * 默认值：1024
     */
    @Value("${thread.pool.queueCapacity:1024}")
    int queueCapacity;

    /**
     * 线程存活时间（秒）
     * 默认值：60
     */
    @Value("${thread.pool.keepAliveSeconds:60}")
    int keepAliveSeconds;

    /**
     * 配置线程池任务执行器
     * 创建一个ThreadPoolTaskExecutor实例，用于处理异步任务
     * 
     * 配置说明：
     * 1. 核心线程数和最大线程数相同，避免线程数动态变化
     * 2. 使用有界队列，防止任务堆积
     * 3. 使用CallerRunsPolicy拒绝策略，防止任务丢失
     * 4. 设置线程名前缀，便于问题排查
     *
     * @return 配置好的线程池任务执行器
     */
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        pool.setCorePoolSize(corePoolSize);
        // 设置最大线程数
        pool.setMaxPoolSize(maxPoolSize);
        // 设置队列容量
        pool.setQueueCapacity(queueCapacity);
        // 设置线程存活时间
        pool.setKeepAliveSeconds(keepAliveSeconds);
        // 设置线程名前缀
        pool.setThreadNamePrefix("gateway-executor-");
        // 设置拒绝策略为CallerRunsPolicy，防止任务丢失
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池
        pool.initialize();
        return pool;
    }

    /**
     * 配置异步任务异常处理器
     * 使用SimpleAsyncUncaughtExceptionHandler处理异步任务中的未捕获异常
     * 该处理器会将异常信息记录到日志中
     *
     * @return 异步任务异常处理器
     */
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}

