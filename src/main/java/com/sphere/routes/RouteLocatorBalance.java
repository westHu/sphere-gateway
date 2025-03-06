package com.sphere.routes;

import com.sphere.GatewayConstant;
import com.sphere.enums.ServiceCodeEnum;
import com.sphere.filter.balance.production.BalanceRequestGatewayFilter;
import com.sphere.filter.balance.production.BalanceResponseGatewayFilter;
import com.sphere.filter.balance.sandbox.SandboxBalanceRequestGatewayFilter;
import com.sphere.filter.balance.sandbox.SandboxBalanceResponseGatewayFilter;
import jakarta.annotation.Resource;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorBalance {

    @Resource
    SandboxBalanceRequestGatewayFilter sandboxBalanceRequestGatewayFilter;
    @Resource
    SandboxBalanceResponseGatewayFilter sandboxBalanceResponseGatewayFilter;
    @Resource
    BalanceRequestGatewayFilter balanceRequestGatewayFilter;
    @Resource
    BalanceResponseGatewayFilter balanceResponseGatewayFilter;
    @Resource
    HostConfiguration hostConfiguration;


    /**
     * 余额查询
     */
    @Bean
    public RouteLocator balanceRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes().route("balanceRouteLocator", r -> r.path(ServiceCodeEnum.INQUIRY_BALANCE.getPath())
                        .and()
                        .host(hostConfiguration.getProduct())
                        .filters(f -> f.rewritePath(ServiceCodeEnum.INQUIRY_BALANCE.getPath(), ServiceCodeEnum.INQUIRY_BALANCE.getRewritePath())
                                .filter(balanceRequestGatewayFilter)
                                .filter(balanceResponseGatewayFilter))
                        .uri(GatewayConstant.URL_PAYMENT))
                .build();
    }

}
