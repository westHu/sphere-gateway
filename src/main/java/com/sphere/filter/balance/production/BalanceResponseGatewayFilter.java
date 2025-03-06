package com.sphere.filter.balance.production;

import com.sphere.GatewayConstant;
import com.sphere.filter.AbstractResponseGatewayFilter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;


/**
 * @author west
 */
@Slf4j
@Component
public class BalanceResponseGatewayFilter extends AbstractResponseGatewayFilter
        implements GatewayFilter, Ordered {

    @Resource
    BalanceResponseService balanceResponseService;

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange.mutate().response(decorate(exchange)).build());
    }

    /**
     * modifyBody
     */
    public BiFunction<ServerWebExchange, String, Mono<String>> modifyBody() {
        return (serverWebExchange, raw) -> {
            HttpHeaders headers = serverWebExchange.getResponse().getHeaders();
            headers.add(GatewayConstant.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.add(GatewayConstant.X_TIMESTAMP, ZonedDateTime.of(LocalDateTime.now(), GatewayConstant.ZONE_ID).format(GatewayConstant.DF_1));
            headers.add(GatewayConstant.ORIGIN, GatewayConstant.HEADER_ORIGIN);

            String convert = balanceResponseService.handlerResponse(serverWebExchange, raw);
            return Mono.just(convert);
        };
    }

}

