package com.sphere.infrastructure.filter.status.production;


import com.sphere.infrastructure.filter.AbstractRequestGatewayFilter;
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
 * 状态查询过滤器
 *
 * @author west
 */
@Slf4j
@Component
public class InquiryStatusRequestGatewayFilter extends AbstractRequestGatewayFilter
        implements GatewayFilter, Ordered {


    @Resource
    InquiryStatusRequestService inquiryStatusRequestService;

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
        return (serverWebExchange, raw) -> inquiryStatusRequestService.handlerRequest(serverWebExchange, raw);
    }

}
