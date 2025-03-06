package com.sphere.exception;

import com.sphere.enums.ServiceCodeEnum;
import com.sphere.utils.PlaceholderUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 处理异常
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GatewayException extends RuntimeException {

    private final ServiceCodeEnum serviceCode;
    private final GatewayExceptionCode exceptionCode;
    private final String message;

    public GatewayException(String message) {
        super(message);
        this.serviceCode = ServiceCodeEnum.UNKNOWN;
        this.exceptionCode = GatewayExceptionCode.SERVER_ERROR;
        this.message = message;
    }

    public GatewayException(GatewayExceptionCode exceptionCode, String message) {
        super(message);
        this.serviceCode = ServiceCodeEnum.UNKNOWN;
        this.exceptionCode = exceptionCode;
        this.message = message;
    }

    public GatewayException(ServiceCodeEnum serviceCodeEnum, GatewayExceptionCode exceptionCode) {
        this.serviceCode = serviceCodeEnum;
        this.exceptionCode = exceptionCode;
        this.message = exceptionCode.getMessage();
    }

    public GatewayException(ServiceCodeEnum serviceCodeEnum, GatewayExceptionCode exceptionCode, String message) {
        super(message);
        this.serviceCode = serviceCodeEnum;
        this.exceptionCode = exceptionCode;
        this.message = PlaceholderUtil.resolve(exceptionCode.getMessage(), message);
    }

}
