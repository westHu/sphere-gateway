package com.sphere.filter.transaction.sandbox;


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
 * @author west
 */
@Slf4j
@Component
public class SandboxTransactionPayInRequestGatewayFilter extends AbstractRequestGatewayFilter
        implements GatewayFilter, Ordered {

    @Resource
    SandboxTransactionPayInRequestService sandboxTransactionPayInRequestService;

    @Override
    public int getOrder() {
        return -4;
    }

    /**
     * sandbox request filter
     */
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return buildVoidMono(exchange, chain);
    }

    @Override
    protected BiFunction<ServerWebExchange, String, Mono<String>> modifyBody() {
        return (serverWebExchange, raw) -> sandboxTransactionPayInRequestService.handlerRequest(serverWebExchange, raw);
    }

}
