package com.sphere.config.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.sphere.enums.ServiceCodeEnum;
import com.sphere.exception.GatewayException;
import com.sphere.exception.GatewayExceptionCode;
import com.sphere.response.BaseResponse;
import com.sphere.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.Optional;

import static com.sphere.GatewayConstant.CODE;
import static com.sphere.GatewayConstant.MERCHANT_ID;
import static com.sphere.GatewayConstant.REQUEST_PARAM;

/**
 * 自定义异常处理
 */
@Slf4j
public class CustomWebExceptionHandler extends DefaultErrorWebExceptionHandler {


    public CustomWebExceptionHandler(ErrorAttributes errorAttributes,
                                     ErrorProperties errorProperties,
                                     ApplicationContext applicationContext) {
        super(errorAttributes, new WebProperties.Resources(), errorProperties, applicationContext);
    }

    /**
     * error
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        ServerWebExchange exchange = request.exchange();
        ServerHttpRequest httpRequest = exchange.getRequest();
        String path = RequestUtil.getPath(httpRequest);
        String ip = RequestUtil.getIpAddress(httpRequest);

        Throwable error = super.getError(request);
        log.error("gateway getErrorAttributes:", error);

        BaseResponse response;

        // Response异常
        if (error instanceof ResponseStatusException responseStatusException) {
            HttpStatusCode statusCode = responseStatusException.getStatusCode();
            log.error("gateway exception type: ResponseStatusException, statusCode={}", statusCode);
            if (statusCode.equals(HttpStatus.NOT_FOUND)) { //URL错误
                response = response(GatewayExceptionCode.NOT_FOUND, null);
            } else {
                GatewayExceptionCode exceptionCode = GatewayExceptionCode.SERVER_ERROR;
                response = response(exceptionCode, error.getMessage());
            }

        }

        // 业务异常
        else if (error instanceof GatewayException gatewayException) {
            log.error("gateway exception type: GatewayException");
            GatewayExceptionCode exceptionCode = gatewayException.getExceptionCode();
            response = response(exceptionCode, error.getMessage());

        }

        // 其他异常
        else {
            log.error("gateway exception type: OtherException");
            response = response(GatewayExceptionCode.SERVER_ERROR, error.getMessage());
        }

        //商户ID
        Object merchantIdObj = exchange.getAttributes().get(MERCHANT_ID);
        String merchantId = Optional.ofNullable(merchantIdObj).map(Object::toString).orElse("unknown");

        //参数
        Object requestParamObj = exchange.getAttributes().get(REQUEST_PARAM);
        String requestParam = Optional.ofNullable(requestParamObj).map(Object::toString).orElse("unknown");

        //消息预警
        String msg = "paysphere pay gateway exception => " +
                "\nPath: " + path +
                "\nIP: " + ip +
                "\nMerchantId: " + merchantId +
                "\nRequestParam: " + requestParam +
                "\nError: " + error.getMessage();
        log.error(msg);
        return BeanUtil.beanToMap(response);
    }

    /**
     * routing
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * httpStatus
     */
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        log.warn("getHttpStatus errorAttributes={}",  JSONUtil.toJsonStr(errorAttributes));
        int statusCode = Optional.ofNullable(errorAttributes).map(e -> e.get(CODE))
                .map(String::valueOf)
                .map(e -> e.substring(0, 3))
                .map(Integer::parseInt).orElse(HttpStatus.INTERNAL_SERVER_ERROR.value());

        try {
            return HttpStatus.valueOf(statusCode).value();

        } catch (Exception e) {
            log.error("gateway getHttpStatus exception", e);
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
    }


    /**
     * response message
     */
    private BaseResponse response(GatewayExceptionCode gatewayExceptionCode, String errorMessage) {
        errorMessage = StringUtils.isBlank(errorMessage) ? gatewayExceptionCode.getMessage() : errorMessage;
        if (errorMessage.contains("finishConnect")) {
            errorMessage = "Network congestion, please try again later";
        }

        BaseResponse response = new BaseResponse();
        response.setCode(gatewayExceptionCode.getCode());
        response.setMessage(errorMessage);
        return response;
    }
}

