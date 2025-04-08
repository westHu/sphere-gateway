package com.sphere.common.exception;

import com.sphere.common.enums.ServiceCodeEnum;
import com.sphere.common.utils.PlaceholderUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 网关异常类
 * 用于处理网关服务中的各种异常情况
 * 包含服务代码、异常代码和错误消息
 * 支持多种构造方式，满足不同的异常处理需求
 *
 * @author sphere
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GatewayException extends RuntimeException {

    /**
     * 服务代码
     * 标识发生异常的服务类型
     */
    private final ServiceCodeEnum serviceCode;

    /**
     * 异常代码
     * 标识具体的异常类型
     */
    private final GatewayExceptionCode exceptionCode;

    /**
     * 错误消息
     * 描述异常的具体信息
     */
    private final String message;

    /**
     * 使用错误消息创建网关异常
     * 默认使用未知服务和服务器错误
     *
     * @param message 错误消息
     */
    public GatewayException(String message) {
        super(message);
        this.serviceCode = ServiceCodeEnum.UNKNOWN;
        this.exceptionCode = GatewayExceptionCode.SERVER_ERROR;
        this.message = message;
    }

    /**
     * 使用异常代码和错误消息创建网关异常
     * 默认使用未知服务
     *
     * @param exceptionCode 异常代码
     * @param message 错误消息
     */
    public GatewayException(GatewayExceptionCode exceptionCode, String message) {
        super(message);
        this.serviceCode = ServiceCodeEnum.UNKNOWN;
        this.exceptionCode = exceptionCode;
        this.message = message;
    }

    /**
     * 使用服务代码和异常代码创建网关异常
     * 使用异常代码的默认消息
     *
     * @param serviceCodeEnum 服务代码
     * @param exceptionCode 异常代码
     */
    public GatewayException(ServiceCodeEnum serviceCodeEnum, GatewayExceptionCode exceptionCode) {
        this.serviceCode = serviceCodeEnum;
        this.exceptionCode = exceptionCode;
        this.message = exceptionCode.getMessage();
    }

    /**
     * 使用服务代码、异常代码和错误消息创建网关异常
     * 支持消息模板替换
     *
     * @param serviceCodeEnum 服务代码
     * @param exceptionCode 异常代码
     * @param message 错误消息
     */
    public GatewayException(ServiceCodeEnum serviceCodeEnum, GatewayExceptionCode exceptionCode, String message) {
        super(message);
        this.serviceCode = serviceCodeEnum;
        this.exceptionCode = exceptionCode;
        this.message = PlaceholderUtil.replacePlaceholders(exceptionCode.getMessage(), message);
    }
}
