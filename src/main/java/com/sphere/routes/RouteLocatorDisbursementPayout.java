package com.sphere.routes;

import com.sphere.GatewayConstant;
import com.sphere.enums.ServiceCodeEnum;
import com.sphere.filter.disbursement.production.DisbursementCashOutRequestGatewayFilter;
import com.sphere.filter.disbursement.production.DisbursementCashOutResponseGatewayFilter;
import com.sphere.filter.disbursement.sandbox.SandboxDisbursementCashOutRequestGatewayFilter;
import com.sphere.filter.disbursement.sandbox.SandboxDisbursementCashOutResponseGatewayFilter;
import jakarta.annotation.Resource;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorDisbursementPayout {

    @Resource
    SandboxDisbursementCashOutRequestGatewayFilter sandboxDisbursementCashOutRequestGatewayFilter;
    @Resource
    SandboxDisbursementCashOutResponseGatewayFilter sandboxDisbursementCashOutResponseGatewayFilter;
    @Resource
    DisbursementCashOutRequestGatewayFilter disbursementCashOutRequestGatewayFilter;
    @Resource
    DisbursementCashOutResponseGatewayFilter disbursementCashOutResponseGatewayFilter;
    @Resource
    HostConfiguration hostConfiguration;


    /**
     * 出款-沙箱
     */
    @Bean
    public RouteLocator sandboxDisbursementPayOutRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes().route("sandboxDisbursementPayOutRouteLocator",
                        r -> r.path(ServiceCodeEnum.DISBURSEMENT_PAYOUT.getPath())
                        .and()
                        .host(hostConfiguration.getSandbox())
                        .filters(f -> f.rewritePath(ServiceCodeEnum.DISBURSEMENT_PAYOUT.getPath(),
                                        ServiceCodeEnum.DISBURSEMENT_PAYOUT.getSandboxRewritePath())
                                .filter(sandboxDisbursementCashOutRequestGatewayFilter)
                                .filter(sandboxDisbursementCashOutResponseGatewayFilter))
                        .uri(GatewayConstant.URL_PAYMENT))
                .build();
    }


    /**
     * 出款
     */
    @Bean
    public RouteLocator disbursementPayOutRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes().route("disbursementPayOutRouteLocator", r -> r.path(ServiceCodeEnum.DISBURSEMENT_PAYOUT.getPath())
                        .and()
                        .host(hostConfiguration.getProduct())
                        .filters(f -> f.rewritePath(ServiceCodeEnum.DISBURSEMENT_PAYOUT.getPath(),
                                        ServiceCodeEnum.DISBURSEMENT_PAYOUT.getRewritePath())
                                .filter(disbursementCashOutRequestGatewayFilter)
                                .filter(disbursementCashOutResponseGatewayFilter))
                        .uri(GatewayConstant.URL_PAYMENT))
                .build();
    }


}
