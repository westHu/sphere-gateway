package com.sphere.routes;

import com.sphere.GatewayConstant;
import com.sphere.enums.ServiceCodeEnum;
import com.sphere.filter.transaction.production.TransactionPayInRequestGatewayFilter;
import com.sphere.filter.transaction.production.TransactionPayInResponseGatewayFilter;
import com.sphere.filter.transaction.sandbox.SandboxTransactionPayInRequestGatewayFilter;
import com.sphere.filter.transaction.sandbox.SandboxTransactionPayInResponseGatewayFilter;
import jakarta.annotation.Resource;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorTransactionDeposit {

    @Resource
    SandboxTransactionPayInRequestGatewayFilter sandboxTransactionPayInRequestGatewayFilter;
    @Resource
    SandboxTransactionPayInResponseGatewayFilter sandboxTransactionPayInResponseGatewayFilter;
    @Resource
    TransactionPayInRequestGatewayFilter transactionPayInRequestGatewayFilter;
    @Resource
    TransactionPayInResponseGatewayFilter transactionPayInResponseGatewayFilter;
    @Resource
    HostConfiguration hostConfiguration;

    /**
     * 收款-沙箱
     */
    @Bean
    public RouteLocator sandboxTransactionDepositRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes().route("sandboxTransactionDepositRouteLocator", r -> r.path(ServiceCodeEnum.TRANSACTION_DEPOSIT.getPath())
                        .and()
                        .host(hostConfiguration.getSandbox())
                        .filters(f -> f.rewritePath(ServiceCodeEnum.TRANSACTION_DEPOSIT.getPath(), ServiceCodeEnum.TRANSACTION_DEPOSIT.getSandboxRewritePath())
                                .filter(sandboxTransactionPayInRequestGatewayFilter)
                                .filter(sandboxTransactionPayInResponseGatewayFilter))
                        .uri(GatewayConstant.URL_PAYMENT))
                .build();
    }


    /**
     * 收款
     */
    @Bean
    public RouteLocator transactionDepositRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes().route("transactionDepositRouteLocator", r -> r.path(ServiceCodeEnum.TRANSACTION_DEPOSIT.getPath())
                        .and()
                        .host(hostConfiguration.getProduct())
                        .filters(f -> f.rewritePath(ServiceCodeEnum.TRANSACTION_DEPOSIT.getPath(), ServiceCodeEnum.TRANSACTION_DEPOSIT.getRewritePath())
                                .filter(transactionPayInRequestGatewayFilter)
                                .filter(transactionPayInResponseGatewayFilter))
                        .uri(GatewayConstant.URL_PAYMENT))
                .build();
    }


}
