package com.sphere.infrastructure.filter;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.sphere.application.dto.ApiConfigDTO;
import com.sphere.application.service.MerchantConfigService;
import com.sphere.common.constants.GatewayConstant;
import com.sphere.common.enums.ServiceCodeEnum;
import com.sphere.common.exception.GatewayException;
import com.sphere.common.exception.GatewayExceptionCode;
import com.sphere.common.utils.RequestUtil;
import com.sphere.common.utils.SignUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 抽象请求服务类
 * 提供请求处理的基础功能，包括参数验证、签名验证等
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractRequestService {

    @Resource
    protected MerchantConfigService merchantConfigService;

    /**
     * 验证请求参数和签名
     * 包括以下验证：
     * 1. 基本参数验证（host、contentType、authorization等）
     * 2. 时间戳验证
     * 3. JWT token验证
     * 4. 签名验证
     *
     * @param serverWebExchange 请求上下文
     * @param raw 原始请求数据
     * @param method 方法名称（用于日志）
     * @return 验证结果
     */
    protected Mono<String> verifyParameterThenSignature(ServerWebExchange serverWebExchange, String raw, String method) {
        // 设置请求参数到上下文
        serverWebExchange.getAttributes().put(GatewayConstant.REQUEST_PARAM, raw);

        // 获取请求信息
        RequestContext requestContext = extractRequestContext(serverWebExchange, method);
        
        // 验证基本参数
        validateBasicParameters(requestContext);
        
        // 验证时间戳
        validateTimestamp(requestContext);
        
        // 验证JWT token
        validateJwtToken(requestContext);
        
        // 验证签名
        return validateSignature(serverWebExchange, raw, requestContext);
    }

    /**
     * 请求上下文数据类
     */
    private static class RequestContext {
        private final String path;
        private final String hostName;
        private final String contentType;
        private final String authorization;
        private final String timestamp;
        private final String signature;
        private final String partnerId;
        private final ServiceCodeEnum serviceCode;
        private final String method;

        public RequestContext(String path, String hostName, String contentType, 
                           String authorization, String timestamp, String signature, 
                           String partnerId, ServiceCodeEnum serviceCode, String method) {
            this.path = path;
            this.hostName = hostName;
            this.contentType = contentType;
            this.authorization = authorization;
            this.timestamp = timestamp;
            this.signature = signature;
            this.partnerId = partnerId;
            this.serviceCode = serviceCode;
            this.method = method;
        }
    }

    /**
     * 提取请求上下文信息
     */
    private RequestContext extractRequestContext(ServerWebExchange exchange, String method) {
        ServerHttpRequest request = exchange.getRequest();
        String path = RequestUtil.getPath(request);
        String hostName = RequestUtil.getHost(request);
        String contentType = RequestUtil.getContentType(request);
        String authorization = RequestUtil.getAuthorization(request);
        String timestamp = RequestUtil.getTimestamp(request);
        String signature = RequestUtil.getSignature(request);
        String partnerId = RequestUtil.getPartnerId(request);
        ServiceCodeEnum serviceCode = ServiceCodeEnum.pathToEnum(path);

        logRequestInfo(method, authorization, timestamp, signature, partnerId);

        return new RequestContext(path, hostName, contentType, authorization, 
                                timestamp, signature, partnerId, serviceCode, method);
    }

    /**
     * 记录请求信息日志
     */
    private void logRequestInfo(String method, String authorization, String timestamp, 
                              String signature, String partnerId) {
        log.info("{} request authorization={}", method, authorization);
        log.info("{} request timestamp={}", method, timestamp);
        log.info("{} request signature={}", method, signature);
        log.info("{} request partnerId={}", method, partnerId);
    }

    /**
     * 验证基本参数
     */
    private void validateBasicParameters(RequestContext context) {
        if (StringUtils.isBlank(context.hostName)) {
            throw new GatewayException(context.serviceCode, GatewayExceptionCode.BAD_REQUEST);
        }

        if (StringUtils.isBlank(context.contentType)) {
            throw new GatewayException(context.serviceCode, GatewayExceptionCode.BAD_REQUEST, 
                                     GatewayConstant.CONTENT_TYPE);
        }

        if (StringUtils.isBlank(context.authorization)) {
            throw new GatewayException(context.serviceCode, GatewayExceptionCode.BAD_REQUEST, 
                                     GatewayConstant.AUTHORIZATION);
        }
    }

    /**
     * 验证时间戳
     */
    private void validateTimestamp(RequestContext context) {
        if (StringUtils.isBlank(context.timestamp)) {
            throw new GatewayException(context.serviceCode, GatewayExceptionCode.BAD_REQUEST, 
                                     GatewayConstant.X_TIMESTAMP);
        }

        try {
            LocalDateTime requestTime = LocalDateTime.parse(context.timestamp, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(requestTime, now);

            if (duration.toMinutes() > 5) {
                log.error("{} request timestamp expired. requestTime={}, now={}", 
                         context.method, requestTime, now);
                throw new GatewayException(context.serviceCode, GatewayExceptionCode.BAD_REQUEST, 
                                         "Timestamp expired");
            }
        } catch (Exception e) {
            log.error("{} Invalid timestamp format: {}", context.method, context.timestamp);
            throw new GatewayException(context.serviceCode, GatewayExceptionCode.BAD_REQUEST, 
                                     "Invalid timestamp format");
        }
    }

    /**
     * 验证JWT token
     */
    private void validateJwtToken(RequestContext context) {
        if (StringUtils.isBlank(context.authorization)) {
            throw new GatewayException(context.serviceCode, GatewayExceptionCode.UNAUTHORIZED);
        }

        if (!context.authorization.startsWith(GatewayConstant.BEARER)) {
            throw new GatewayException(context.serviceCode, GatewayExceptionCode.BAD_REQUEST, 
                                     GatewayConstant.AUTHORIZATION);
        }

        String jwtToken = context.authorization.replace(GatewayConstant.BEARER, "").trim();
        try {
            JWT jwt = JWTUtil.parseToken(jwtToken);
            String key = SecureUtil.sha256("sphere");
            if (!jwt.setKey(key.getBytes(StandardCharsets.UTF_8)).verify()) {
                log.error("{} request jwt verify failed", context.method);
                throw new GatewayException(context.serviceCode, GatewayExceptionCode.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.error("{} JWT verification failed", context.method, e);
            throw new GatewayException(context.serviceCode, GatewayExceptionCode.UNAUTHORIZED);
        }
    }

    /**
     * 验证签名
     */
    private Mono<String> validateSignature(ServerWebExchange exchange, String raw, RequestContext context) {
        return merchantConfigService.getApiConfigDTO(context.partnerId, context.hostName)
                .flatMap(configDTO -> {
                    if (Objects.isNull(configDTO) || StringUtils.isBlank(configDTO.getMerchantSecret())) {
                        log.error("{} Merchant config not exist. merchantId={}", 
                                context.method, context.partnerId);
                        return Mono.error(new GatewayException(context.serviceCode, 
                                     GatewayExceptionCode.UNAUTHORIZED));
                    }

                    String stringToSign = SignUtil.stringToSign(context.path, 
                            context.authorization.replace(GatewayConstant.BEARER, "").trim(), 
                            raw, context.timestamp);
                    String calculatedSignature = SignUtil.hmacSHA512(stringToSign, 
                            configDTO.getMerchantSecret());

                    if (!calculatedSignature.equals(context.signature)) {
                        log.error("{} request signature verify failed. expected={}, actual={}", 
                                context.method, calculatedSignature, context.signature);
                        return Mono.error(new GatewayException(context.serviceCode, 
                                     GatewayExceptionCode.UNAUTHORIZED));
                    }

                    return Mono.just(raw);
                });
    }

    /**
     * WooCommerce 校验参数、校验签名
     */
    protected Mono<String> verifyWooCommerceSignature(ServerWebExchange serverWebExchange, String raw) {
        //获取参数
        ServerHttpRequest request = serverWebExchange.getRequest();
        log.info("WooCommercePayInRequestService request={}", request.getQueryParams().toSingleValueMap());
        String path = RequestUtil.getPath(request);
        String hostName = RequestUtil.getHost(request);
        ServiceCodeEnum serviceCodeEnum = ServiceCodeEnum.pathToEnum(path);


        //校验参数
        if (StringUtils.isBlank(hostName)) {
            throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.BAD_REQUEST);
            //return Mono.error(new GatewayException(serviceCodeEnum, ResponseExceptionCode.BAD_REQUEST));
        }

        JSONObject jsonObject = JSONUtil.parseObj(raw);
        String merchantId = jsonObject.getStr("merchantId");

        //查询商户配置 WooCommercePayInRequestService
        Mono<ApiConfigDTO> configDTOMono = merchantConfigService.getApiConfigDTO(merchantId, hostName);
        return configDTOMono.flatMap(configDTO -> {
            if (Objects.isNull(configDTO) || StringUtils.isBlank(configDTO.getMerchantSecret())) {
                log.error("WooCommercePayInRequestService Merchant  config not exist");
                throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.UNAUTHORIZED);
                //return Mono.error(new GatewayException(serviceCodeEnum, ResponseExceptionCode.UNAUTHORIZED));
            }
            String merchantSecret = configDTO.getMerchantSecret();

            //验证Authorization
            // $signature = md5($this->merchantId . $this->prefix . $order_id . $totalAmount . $this->merchantSecret);
            String signature = jsonObject.getStr("signature");
            String merchantOrderId = jsonObject.getStr("merchantOrderId");
            String paymentAmount = jsonObject.getStr("paymentAmount");
            String content = merchantId + merchantOrderId + paymentAmount + merchantSecret;
            String calculateMd5 = SecureUtil.md5(content);
            if (!signature.equals(calculateMd5)) {
                log.error("content ={} \n signature={} \n calculateMd5={}", content, signature, calculateMd5);
                throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.UNAUTHORIZED, "Access Signature Invalid");
                //return Mono.error(new GatewayException(serviceCodeEnum, ResponseExceptionCode.UNAUTHORIZED, "Access Signature Invalid"));
            }

            return Mono.just(raw);
        });
    }

}
