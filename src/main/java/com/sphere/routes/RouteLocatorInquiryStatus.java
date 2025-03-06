package com.sphere.routes;

import com.sphere.GatewayConstant;
import com.sphere.enums.ServiceCodeEnum;
import com.sphere.filter.status.production.InquiryStatusRequestGatewayFilter;
import com.sphere.filter.status.production.InquiryStatusResponseGatewayFilter;
import com.sphere.filter.status.sandbox.SandboxInquiryStatusRequestGatewayFilter;
import com.sphere.filter.status.sandbox.SandboxInquiryStatusResponseGatewayFilter;
import jakarta.annotation.Resource;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorInquiryStatus {

    @Resource
    SandboxInquiryStatusRequestGatewayFilter sandboxInquiryStatusRequestGatewayFilter;
    @Resource
    SandboxInquiryStatusResponseGatewayFilter sandboxInquiryStatusResponseGatewayFilter;
    @Resource
    InquiryStatusRequestGatewayFilter inquiryStatusRequestGatewayFilter;
    @Resource
    InquiryStatusResponseGatewayFilter inquiryStatusResponseGatewayFilter;
    @Resource
    HostConfiguration hostConfiguration;

    /**
     * 查询订单状态-沙箱
     */
    @Bean
    public RouteLocator sandboxInquiryStatusRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes().route("sandboxInquiryStatusRouteLocator", r -> r.path(ServiceCodeEnum.INQUIRY_STATUS.getPath())
                        .and()
                        .host(hostConfiguration.getSandbox())
                        .filters(f -> f.rewritePath(ServiceCodeEnum.INQUIRY_STATUS.getPath(), ServiceCodeEnum.INQUIRY_STATUS.getSandboxRewritePath())
                                .filter(sandboxInquiryStatusRequestGatewayFilter)
                                .filter(sandboxInquiryStatusResponseGatewayFilter))
                        .uri(GatewayConstant.URL_PAYMENT))
                .build();
    }


    /**
     * 查询订单状态
     */
    @Bean
    public RouteLocator inquiryStatusRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes().route("inquiryStatusRouteLocator", r -> r.path(ServiceCodeEnum.INQUIRY_STATUS.getPath())
                        .and()
                        .host(hostConfiguration.getProduct())
                        .filters(f -> f.rewritePath(ServiceCodeEnum.INQUIRY_STATUS.getPath(), ServiceCodeEnum.INQUIRY_STATUS.getRewritePath())
                                .filter(inquiryStatusRequestGatewayFilter)
                                .filter(inquiryStatusResponseGatewayFilter))
                        .uri(GatewayConstant.URL_PAYMENT))
                .build();
    }

}
