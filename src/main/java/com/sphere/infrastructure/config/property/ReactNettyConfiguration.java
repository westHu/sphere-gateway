package com.sphere.infrastructure.config.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;

/**
 * Reactor Netty 配置类
 * 配置 Netty 的 IO 线程模型，优化网关的网络性能
 * 
 * 主要配置：
 * 1. IO 选择器数量：负责接受新的连接
 * 2. IO 工作线程数：负责处理 IO 事件
 * 3. 连接池策略：使用 LIFO 策略提高性能
 * 
 * 性能优化说明：
 * 1. 选择器数量通常设置为 CPU 核心数
 * 2. 工作线程数建议设置为 CPU 核心数的 2-4 倍
 * 3. 使用 LIFO 策略可以减少线程切换开销
 * 4. 根据实际负载情况调整参数
 *
 * @author sphere
 * @since 1.0.0
 */
@Configuration
public class ReactNettyConfiguration {

    /**
     * IO 选择器数量
     * 负责接受新的连接请求
     * 默认值：1
     */
    @Value("${reactor.netty.select-count:1}")
    String selectCount;

    /**
     * IO 工作线程数
     * 负责处理 IO 事件
     * 默认值：64
     */
    @Value("${reactor.netty.worker-count:64}")
    String workerCount;

    /**
     * 配置 Reactor Netty 资源工厂
     * 设置 Netty 的 IO 线程模型参数
     * 
     * 配置说明：
     * 1. 使用 LIFO 策略优化连接池性能
     * 2. 根据 CPU 核心数设置选择器数量
     * 3. 根据负载情况设置工作线程数
     * 4. 支持通过配置文件动态调整
     *
     * @return 配置好的 Reactor 资源工厂
     */
    @Bean
    public ReactorResourceFactory reactorClientResourceFactory() {
        // 设置连接池策略为 LIFO，提高性能
        System.setProperty("reactor.netty.pool.leasingStrategy", "lifo");
        
        // 设置 IO 选择器数量
        System.setProperty("reactor.netty.ioSelectCount", selectCount);
        
        // 设置 IO 工作线程数
        System.setProperty("reactor.netty.ioWorkerCount", workerCount);
        
        // 创建并返回资源工厂
        return new ReactorResourceFactory();
    }
}