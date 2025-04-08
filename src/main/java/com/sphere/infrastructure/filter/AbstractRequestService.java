package com.sphere.infrastructure.filter;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.sphere.application.dto.ApiConfigDTO;
import com.sphere.application.service.MerchantConfigService;
import com.sphere.common.constants.GatewayConstant;
import com.sphere.common.enums.ServiceCodeEnum;;
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
import java.util.Objects;

@Slf4j
public abstract class AbstractRequestService {

    @Resource
    MerchantConfigService merchantConfigService;

    /**
     * 校验参数、校验签名
     */
    protected Mono<String> verifyParameterThenSignature(ServerWebExchange serverWebExchange, String raw, String method) {
        //参数设置到Attribute
        serverWebExchange.getAttributes().put(GatewayConstant.REQUEST_PARAM, raw);

        ServerHttpRequest request = serverWebExchange.getRequest();
        String path = RequestUtil.getPath(request);
        String hostName = RequestUtil.getHost(request);
        String contentType = RequestUtil.getContentType(request);
        String authorization = RequestUtil.getAuthorization(request);
        String timestamp = RequestUtil.getTimestamp(request);
        String signature = RequestUtil.getSignature(request);
        String partnerId = RequestUtil.getPartnerId(request);
        //String origin = RequestUtil.getOrigin(request);
        //String externalId = RequestUtil.getExternalId(request);
        //String channelId = RequestUtil.getChannelId(request);
        ServiceCodeEnum serviceCodeEnum = ServiceCodeEnum.pathToEnum(path);
        log.info("{} request authorization={}", method, authorization);
        log.info("{} request timestamp={}", method, timestamp);
        log.info("{} request signature={}", method, signature);
        log.info("{} request partnerId={}", method, partnerId);

        //校验参数
        //校验Host非空
        if (StringUtils.isBlank(hostName)) {
            throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.BAD_REQUEST);
        }

        //校验时间戳非空
        if (StringUtils.isBlank(contentType)) {
            throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.BAD_REQUEST, GatewayConstant.CONTENT_TYPE);
        }

        //校验accessToken加签非空
        if (StringUtils.isBlank(authorization)) {
            throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.BAD_REQUEST, GatewayConstant.AUTHORIZATION);
        }
        //校验authorization的格式
        if (!authorization.startsWith(GatewayConstant.BEARER)) {
            throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.BAD_REQUEST, GatewayConstant.AUTHORIZATION);
        }

        //校验accessToken加签非空
        if (StringUtils.isBlank(timestamp)) {
            throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.BAD_REQUEST, GatewayConstant.X_TIMESTAMP);
        }

        //校验X_SIGNATURE加签非空
        if (StringUtils.isBlank(signature)) {
            throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.BAD_REQUEST, GatewayConstant.X_SIGNATURE);
        }

        //校验X_PARTNER_ID加签非空
        if (StringUtils.isBlank(partnerId)) {
            throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.BAD_REQUEST, GatewayConstant.X_PARTNER_ID);
        }

        //校验时间戳格式
        long seconds;
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, GatewayConstant.DF_1);
            LocalDateTime now = LocalDateTime.now();
            seconds = Duration.between(dateTime, now).getSeconds();
            log.info("{} Timestamp format dateTime={}, now={}, seconds={}", method, dateTime, now, seconds);
        } catch (Exception e) {
            log.error("{} Timestamp format exception", method, e);
            throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.BAD_REQUEST, GatewayConstant.X_TIMESTAMP);
        }
        if (seconds > 60 * 60) {
            throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.BAD_REQUEST, "timestamp is expired.");
        }


        //查询商户配置 partnerId = clientID = merchantID
        Mono<ApiConfigDTO> configDTOMono = merchantConfigService.getIntersectionConfigDTO(partnerId, hostName);
        return configDTOMono.flatMap(configDTO -> {
            log.info("{} configDTO={}", method, JSONUtil.toJsonStr(configDTO));

            if (Objects.isNull(configDTO) || StringUtils.isBlank(configDTO.getMerchantSecret())) {
                log.error("{} Merchant config not exist", method);
                throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.UNAUTHORIZED);
                //return Mono.error(new GatewayException(serviceCodeEnum, ResponseExceptionCode.UNAUTHORIZED));
            }

            String merchantSecret = configDTO.getMerchantSecret();

            //验证token 也就是Authorization
            boolean validate;
            String jwtToken = authorization.replace(GatewayConstant.BEARER, "").trim();
            try {
                JWT jwt = JWTUtil.parseToken(jwtToken);
                String key = SecureUtil.sha256("sss");
                validate = jwt.setKey(key.getBytes(StandardCharsets.UTF_8)).validate(0);
            } catch (Exception e) {
                log.error("{} JWT verify exception", method, e);
                throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.UNAUTHORIZED, "Access Token Validate Failed");
                //return Mono.error(new GatewayException(serviceCodeEnum, ResponseExceptionCode.UNAUTHORIZED, Access Token Validate Failed"));
            }

            if (!validate) {
                log.error("{} JWT verify failed. jwtToken={}", method, jwtToken);
                throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.UNAUTHORIZED, "Access Token Expiry");
                //return Mono.error(new GatewayException(serviceCodeEnum, ResponseExceptionCode.UNAUTHORIZED, Access Token Expiry"));
            }

            //校验签名 对称签名
            String stringToSign = SignUtil.stringToSign(path, jwtToken, raw, timestamp);
            log.info("{} signature verify. merchantSecret={} \nstringToSign={}", method, merchantSecret, stringToSign);

            String paysphereSign = SignUtil.hmacSHA512(stringToSign, merchantSecret);
            if (!signature.equals(paysphereSign)) {
                log.error("{} signature verify failed, \n paysphereSign={},\n merchantSign={}", method, paysphereSign, signature);
                throw new GatewayException(serviceCodeEnum, GatewayExceptionCode.UNAUTHORIZED, "Access Signature Invalid");
                //return Mono.error(new GatewayException(ResponseExceptionCode.UNAUTHORIZED, serviceCodeEnum, Access Signature Invalid"));
            }

            log.info("{} signature verify pass", method);
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
        Mono<ApiConfigDTO> configDTOMono = merchantConfigService.getIntersectionConfigDTO(merchantId, hostName);
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
