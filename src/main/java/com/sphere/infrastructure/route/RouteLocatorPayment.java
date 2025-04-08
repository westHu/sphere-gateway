package com.sphere.infrastructure.route;

import com.sphere.common.constants.GatewayConstant;
import com.sphere.infrastructure.filter.payment.PaymentRequestGatewayFilter;
import jakarta.annotation.Resource;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorPayment {

    @Resource
    HostConfiguration hostConfiguration;
    @Resource
    PaymentRequestGatewayFilter paymentRequestGatewayFilter;

    /**
     * 渠道相关
     */
    @Bean
    public RouteLocator paymentRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes().route("paymentRouteLocator", r -> r.path("/payment/**")
                        .and()
                        .host(hostConfiguration.getProduct())
                        .filters(f -> f.filter(paymentRequestGatewayFilter))
                        .uri(GatewayConstant.URL_PAYMENT))
                .build();
    }

}
