package com.sphere.infrastructure.config.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.sphere.common.exception.GatewayException;
import com.sphere.common.exception.GatewayExceptionCode;
import com.sphere.api.vo.BaseResult;
import com.sphere.common.utils.RequestUtil;
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

import static com.sphere.common.constants.GatewayConstant.CODE;
import static com.sphere.common.constants.GatewayConstant.MERCHANT_ID;
import static com.sphere.common.constants.GatewayConstant.REQUEST_PARAM;

/**
 * 自定义Web异常处理器
 * 处理网关中的各种异常，包括：
 * 1. 响应状态异常（如404）
 * 2. 业务异常（GatewayException）
 * 3. 其他系统异常
 * 统一返回标准格式的错误响应
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
public class CustomWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    /**
     * 构造函数
     *
     * @param errorAttributes 错误属性
     * @param errorProperties 错误配置
     * @param applicationContext 应用上下文
     */
    public CustomWebExceptionHandler(ErrorAttributes errorAttributes,
                                     ErrorProperties errorProperties,
                                     ApplicationContext applicationContext) {
        super(errorAttributes, new WebProperties.Resources(), errorProperties, applicationContext);
    }

    /**
     * 获取错误属性
     * 根据异常类型返回不同的错误响应
     *
     * @param request 请求对象
     * @param options 错误属性选项
     * @return 错误属性Map
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        ServerWebExchange exchange = request.exchange();
        ServerHttpRequest httpRequest = exchange.getRequest();
        String path = RequestUtil.getPath(httpRequest);
        String ip = RequestUtil.getIpAddress(httpRequest);

        Throwable error = super.getError(request);
        log.error("网关异常处理 - 路径: {}, IP: {}, 错误: {}", path, ip, error.getMessage(), error);

        BaseResult response;

        // 处理响应状态异常
        if (error instanceof ResponseStatusException responseStatusException) {
            HttpStatusCode statusCode = responseStatusException.getStatusCode();
            log.error("响应状态异常 - 状态码: {}", statusCode);
            if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                response = response(GatewayExceptionCode.NOT_FOUND, null);
            } else {
                response = response(GatewayExceptionCode.SERVER_ERROR, error.getMessage());
            }
        }
        // 处理业务异常
        else if (error instanceof GatewayException gatewayException) {
            log.error("业务异常 - 异常码: {}", gatewayException.getExceptionCode());
            GatewayExceptionCode exceptionCode = gatewayException.getExceptionCode();
            response = response(exceptionCode, error.getMessage());
        }
        // 处理其他异常
        else {
            log.error("系统异常 - 类型: {}", error.getClass().getName());
            response = response(GatewayExceptionCode.SERVER_ERROR, error.getMessage());
        }

        // 获取商户ID
        Object merchantIdObj = exchange.getAttributes().get(MERCHANT_ID);
        String merchantId = Optional.ofNullable(merchantIdObj).map(Object::toString).orElse("unknown");

        // 获取请求参数
        Object requestParamObj = exchange.getAttributes().get(REQUEST_PARAM);
        String requestParam = Optional.ofNullable(requestParamObj).map(Object::toString).orElse("unknown");

        // 记录详细错误日志
        String msg = String.format("""
                网关异常详情 =>
                路径: %s
                IP: %s
                商户ID: %s
                请求参数: %s
                错误信息: %s""", 
                path, ip, merchantId, requestParam, error.getMessage());
        log.error(msg);

        return BeanUtil.beanToMap(response);
    }

    /**
     * 获取路由函数
     * 配置错误处理的路由
     *
     * @param errorAttributes 错误属性
     * @return 路由函数
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 获取HTTP状态码
     * 根据错误属性中的code字段确定HTTP状态码
     *
     * @param errorAttributes 错误属性
     * @return HTTP状态码
     */
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        log.debug("获取HTTP状态码 - 错误属性: {}", JSONUtil.toJsonStr(errorAttributes));
        
        int statusCode = Optional.ofNullable(errorAttributes)
                .map(e -> e.get(CODE))
                .map(String::valueOf)
                .map(e -> e.substring(0, 3))
                .map(Integer::parseInt)
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value());

        try {
            return HttpStatus.valueOf(statusCode).value();
        } catch (Exception e) {
            log.error("获取HTTP状态码异常", e);
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
    }

    /**
     * 构建错误响应
     * 根据异常码和错误消息构建标准响应对象
     *
     * @param gatewayExceptionCode 网关异常码
     * @param errorMessage 错误消息
     * @return 标准响应对象
     */
    private BaseResult response(GatewayExceptionCode gatewayExceptionCode, String errorMessage) {
        // 处理错误消息
        errorMessage = StringUtils.isBlank(errorMessage) ? gatewayExceptionCode.getMessage() : errorMessage;
        if (errorMessage.contains("finishConnect")) {
            errorMessage = "Network congestion, please try again later";
        }

        // 构建响应对象
        BaseResult response = new BaseResult();
        response.setCode(Integer.parseInt(gatewayExceptionCode.getCode()));
        response.setMessage(errorMessage);
        return response;
    }
}

