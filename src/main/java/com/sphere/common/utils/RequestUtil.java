package com.sphere.common.utils;

import com.sphere.common.constants.GatewayConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Slf4j
public class RequestUtil {

    /**
     * host
     */
    public static String getHost(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.HOST_HEADER_NAME);
    }

    /**
     * path
     */
    public static String getPath(ServerHttpRequest request) {
        return request.getPath().value();
    }

    /**
     * real ip
     */
    public static String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.IP_HEADER_NAME);
    }

    /**
     * CONTENT_TYPE
     */
    public static String getContentType(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.CONTENT_TYPE);
    }

    /**
     * AUTHORIZATION
     */
    public static String getAuthorization(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.AUTHORIZATION);
    }

    /**
     * X_TIMESTAMP
     */
    public static String getTimestamp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.X_TIMESTAMP);
    }

    /**
     * X_SIGNATURE
     */
    public static String getSignature(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.X_SIGNATURE);
    }

    /**
     * X_PARTNER_ID
     */
    public static String getPartnerId(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.X_PARTNER_ID);
    }

}