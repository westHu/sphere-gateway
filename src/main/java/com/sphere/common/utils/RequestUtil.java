package com.sphere.common.utils;

import com.sphere.common.constants.GatewayConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * 请求工具类
 * 用于处理HTTP请求相关的操作
 * 提供获取请求头、路径、IP等信息的工具方法
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
public class RequestUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private RequestUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取请求的Host信息
     * 从请求头中获取Host字段的值
     *
     * @param request HTTP请求对象
     * @return Host值，如果不存在则返回null
     */
    public static String getHost(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.HOST_HEADER_NAME);
    }

    /**
     * 获取请求的路径
     * 获取完整的请求URI路径
     *
     * @param request HTTP请求对象
     * @return 请求路径
     */
    public static String getPath(ServerHttpRequest request) {
        return request.getPath().value();
    }

    /**
     * 获取请求的真实IP地址
     * 从请求头中获取CF-Connecting-IP字段的值
     *
     * @param request HTTP请求对象
     * @return IP地址，如果不存在则返回null
     */
    public static String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.IP_HEADER_NAME);
    }

    /**
     * 获取请求的内容类型
     * 从请求头中获取Content-Type字段的值
     *
     * @param request HTTP请求对象
     * @return 内容类型，如果不存在则返回null
     */
    public static String getContentType(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.CONTENT_TYPE);
    }

    /**
     * 获取请求的授权信息
     * 从请求头中获取Authorization字段的值
     *
     * @param request HTTP请求对象
     * @return 授权信息，如果不存在则返回null
     */
    public static String getAuthorization(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.AUTHORIZATION);
    }

    /**
     * 获取请求的时间戳
     * 从请求头中获取X-TIMESTAMP字段的值
     *
     * @param request HTTP请求对象
     * @return 时间戳，如果不存在则返回null
     */
    public static String getTimestamp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.X_TIMESTAMP);
    }

    /**
     * 获取请求的签名
     * 从请求头中获取X-SIGNATURE字段的值
     *
     * @param request HTTP请求对象
     * @return 签名，如果不存在则返回null
     */
    public static String getSignature(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.X_SIGNATURE);
    }

    /**
     * 获取请求的合作伙伴ID
     * 从请求头中获取X-PARTNER-ID字段的值
     *
     * @param request HTTP请求对象
     * @return 合作伙伴ID，如果不存在则返回null
     */
    public static String getPartnerId(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(GatewayConstant.X_PARTNER_ID);
    }
}