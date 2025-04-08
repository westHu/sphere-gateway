package com.sphere.infrastructure.config.web;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * WebClient 配置类
 * 配置 HTTP 客户端，包括连接池、超时设置和重试机制
 * 支持负载均衡
 * 
 * 主要功能：
 * 1. 负载均衡支持
 * 2. 连接池配置
 * 3. 超时设置
 * 4. 重试机制
 * 5. 错误处理
 * 
 * 配置说明：
 * 1. 最大连接数：200
 * 2. 连接超时：5秒
 * 3. 读写超时：10秒
 * 4. 重试次数：3次
 *
 * @author sphere
 * @since 1.0.0
 */
@Configuration
public class WebClientConfig {

    /**
     * 配置支持负载均衡的 WebClient.Builder
     * 设置连接池、超时和重试机制
     *
     * @return 配置好的 WebClient.Builder 实例
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        // 配置连接池
        ConnectionProvider provider = ConnectionProvider.builder("custom")
                .maxConnections(200)                // 最大连接数
                .pendingAcquireTimeout(Duration.ofSeconds(60)) // 等待连接超时
                .maxLifeTime(Duration.ofMinutes(5)) // 连接最大生命周期
                .build();

        // 配置 HTTP 客户端
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 连接超时
                .responseTimeout(Duration.ofSeconds(10))            // 响应超时
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(10))  // 读取超时
                        .addHandlerLast(new WriteTimeoutHandler(10)) // 写入超时
                );

        // 创建 WebClient.Builder
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(errorHandler())    // 添加错误处理
                .filter(retryFilter());    // 添加重试机制
    }

    /**
     * 错误处理过滤器
     * 处理 HTTP 请求过程中的错误
     *
     * @return ExchangeFilterFunction 实例
     */
    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException(
                                "Server error: " + clientResponse.statusCode() + ", body: " + body)));
            }
            if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException(
                                "Client error: " + clientResponse.statusCode() + ", body: " + body)));
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * 重试过滤器
     * 对失败的请求进行重试
     *
     * @return ExchangeFilterFunction 实例
     */
    private ExchangeFilterFunction retryFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            return Mono.just(clientRequest)
                    .retry(3) // 重试3次
                    .doOnError(throwable -> {
                        // 记录重试失败日志
                        System.err.println("Retry failed after 3 attempts: " + throwable.getMessage());
                    });
        });
    }
}
