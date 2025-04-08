package com.sphere.infrastructure.config.web;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * 动态WebClient配置类
 * 支持运行时动态更新服务地址
 * 包含完整的错误处理、重试机制和监控
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class DynamicWebClientConfig {

    @Value("${webclient.connection.max-connections:200}")
    private int maxConnections;

    @Value("${webclient.connection.pending-acquire-timeout:60}")
    private int pendingAcquireTimeout;

    @Value("${webclient.connection.max-life-time:5}")
    private int maxLifeTime;

    @Value("${webclient.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${webclient.timeout.response:10}")
    private int responseTimeout;

    @Value("${webclient.timeout.read:10}")
    private int readTimeout;

    @Value("${webclient.timeout.write:10}")
    private int writeTimeout;

    @Value("${webclient.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${webclient.retry.initial-backoff:100}")
    private long initialBackoff;

    // 动态baseUrl存储
    private final AtomicReference<String> baseUrl = new AtomicReference<>("http://localhost:8080");

    /**
     * 配置支持负载均衡的WebClient.Builder
     * 包含连接池、超时、重试等配置
     *
     * @return 配置好的WebClient.Builder实例
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        log.info("初始化WebClient配置 - 最大连接数: {}, 连接超时: {}ms, 响应超时: {}s",
                maxConnections, connectTimeout, responseTimeout);

        // 配置连接池
        ConnectionProvider provider = ConnectionProvider.builder("custom")
                .maxConnections(maxConnections)
                .pendingAcquireTimeout(Duration.ofSeconds(pendingAcquireTimeout))
                .maxLifeTime(Duration.ofMinutes(maxLifeTime))
                .build();

        // 配置HTTP客户端
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofSeconds(responseTimeout))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeout))
                                .addHandlerLast(new WriteTimeoutHandler(writeTimeout))
                );

        // 创建WebClient.Builder
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(errorHandler())
                .filter(retryFilter());
    }

    /**
     * 错误处理过滤器
     * 处理HTTP请求过程中的错误
     *
     * @return ExchangeFilterFunction实例
     */
    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.error("服务器错误 - 状态码: {}, 响应体: {}", 
                                    clientResponse.statusCode(), body);
                            return Mono.error(new RuntimeException(
                                    String.format("服务器错误: %s, 响应体: %s", 
                                            clientResponse.statusCode(), body)));
                        });
            }
            if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.error("客户端错误 - 状态码: {}, 响应体: {}", 
                                    clientResponse.statusCode(), body);
                            return Mono.error(new RuntimeException(
                                    String.format("客户端错误: %s, 响应体: %s", 
                                            clientResponse.statusCode(), body)));
                        });
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * 重试过滤器
     * 使用指数退避策略对失败的请求进行重试
     *
     * @return ExchangeFilterFunction实例
     */
    private ExchangeFilterFunction retryFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            return Mono.just(clientRequest)
                    .retryWhen(reactor.util.retry.Retry.backoff(maxRetryAttempts, 
                            Duration.ofMillis(initialBackoff))
                            .doBeforeRetry(retrySignal -> 
                                log.warn("重试请求 - 第{}次重试, URL: {}", 
                                        retrySignal.totalRetries() + 1, 
                                        clientRequest.url()))
                    )
                    .doOnError(throwable -> 
                        log.error("重试失败 - URL: {}, 错误: {}", 
                                clientRequest.url(), throwable.getMessage()));
        });
    }

    /**
     * 创建新的WebClient实例
     * 使用当前配置的baseUrl
     *
     * @return 新的WebClient实例
     */
    public WebClient createWebClient() {
        return webClientBuilder()
                .baseUrl(baseUrl.get())
                .build();
    }

    /**
     * 更新服务地址
     * 支持运行时动态更新服务地址
     *
     * @param newBaseUrl 新的服务地址
     */
    public void updateBaseUrl(String newBaseUrl) {
        log.info("更新服务地址 - 旧地址: {}, 新地址: {}", baseUrl.get(), newBaseUrl);
        baseUrl.set(newBaseUrl);
    }

    /**
     * 获取当前服务地址
     *
     * @return 当前服务地址
     */
    public String getCurrentBaseUrl() {
        return baseUrl.get();
    }
} 