package com.sphere.filter.payment;

import com.sphere.filter.AbstractRequestGatewayFilter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

/**
 * 交易过滤器
 *
 * @author west
 */
@Slf4j
@Component
public class PaymentRequestGatewayFilter extends AbstractRequestGatewayFilter
        implements GatewayFilter, Ordered {


    @Resource
    PaymentRequestService paymentRequestService;

    @Override
    public int getOrder() {
        return -2;
    }

    /**
     * request filter
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return buildVoidMono(exchange, chain);
    }

    /**
     * modify request body
     */
    public BiFunction<ServerWebExchange, String, Mono<String>> modifyBody() {
        return (serverWebExchange, raw) -> {
            raw = paymentRequestService.handlerRequest(serverWebExchange, raw);
            return Mono.just(raw);
        };
    }

}
