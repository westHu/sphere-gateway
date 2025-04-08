package com.sphere.infrastructure.integration.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

/**
 * 支付服务配置类
 * 使用动态WebClient配置
 * 支持运行时更新服务地址
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class PaymentServiceConfig {

    @Value("${payment.service.timeout:30}")
    private int timeout;

    /**
     * 配置支付服务API客户端
     * 使用动态WebClient配置
     *
     * @param webClientBuilder 注入的WebClient.Builder
     * @return PaymentServiceApi实例
     */
    @Bean
    public PaymentServiceApi paymentServiceApi(WebClient.Builder webClientBuilder) {
        log.info("初始化支付服务API客户端 - 超时: {}秒", timeout);

        // 创建WebClient实例
        WebClient webClient = webClientBuilder.build();

        // 创建HttpServiceProxyFactory
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builder()
                .clientAdapter(WebClientAdapter.forClient(webClient))
                .blockTimeout(Duration.ofSeconds(timeout))
                .build();

        // 创建并返回PaymentServiceApi实例
        PaymentServiceApi paymentServiceApi = factory.createClient(PaymentServiceApi.class);
        log.info("支付服务API客户端初始化完成");
        
        return paymentServiceApi;
    }
} 