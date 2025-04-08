package com.sphere.application.service;

import cn.hutool.json.JSONUtil;
import com.sphere.application.dto.ApiConfigDTO;
import com.sphere.common.constants.GatewayConstant;
import com.sphere.infrastructure.cache.RedisService;
import com.sphere.infrastructure.integration.payment.PaymentServiceApi;
import com.sphere.infrastructure.integration.payment.dto.MerchantConfigDTO;
import com.sphere.infrastructure.integration.payment.dto.SandboxMerchantConfigDTO;
import com.sphere.infrastructure.integration.payment.param.MerchantIdParam;
import com.sphere.api.vo.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class MerchantConfigService {

    @Resource
    PaymentServiceApi merchantService;

    @Resource
    RedisService redisService;


    /**
     * 取商户白名单
     */
    public Mono<ApiConfigDTO> getIntersectionConfigDTO(String merchantId, String hostName) {
        boolean isSandbox = hostName.contains(GatewayConstant.SANDBOX);

        if (isSandbox) {
            Mono<SandboxMerchantConfigDTO> mono = getSandboxMerchantConfigDTO(merchantId);
            return mono.map(configDTO -> {
                ApiConfigDTO apiConfigDTO = new ApiConfigDTO();
                apiConfigDTO.setPublicKey(configDTO.getPublicKey());
                apiConfigDTO.setMerchantSecret(configDTO.getMerchantSecret());
                apiConfigDTO.setIpWhiteList(configDTO.getIpWhiteList());
                return apiConfigDTO;
            });
        } else {
            Mono<MerchantConfigDTO> mono = getMerchantConfigDTO(merchantId);
            return mono.map(configDTO -> {
                ApiConfigDTO apiConfigDTO = new ApiConfigDTO();
                apiConfigDTO.setPublicKey(configDTO.getPublicKey());
                apiConfigDTO.setMerchantSecret(configDTO.getMerchantSecret());
                apiConfigDTO.setExpiryDate(configDTO.getExpiryDate());
                apiConfigDTO.setIpWhiteList(configDTO.getIpWhiteList());
                return apiConfigDTO;
            });
        }
    }

    /**
     * 取沙箱环境 商户配置
     */
    public Mono<SandboxMerchantConfigDTO> getSandboxMerchantConfigDTO(String merchantId) {
        String cacheKey = GatewayConstant.SANDBOX_CACHE_MERCHANT_CONFIG + merchantId;

        // 缓存获取
        Object obj = redisService.get(cacheKey);
        SandboxMerchantConfigDTO configDTO = Optional.ofNullable(obj).map(Object::toString)
                .map(e -> JSONUtil.toBean(e, SandboxMerchantConfigDTO.class))
                .orElse(null);
        if (Objects.nonNull(configDTO)) {
            return Mono.just(configDTO);
        }

        // 微服务获取
        MerchantIdParam param = new MerchantIdParam();
        param.setMerchantId(merchantId);
        Mono<Result<SandboxMerchantConfigDTO>> resultMono = merchantService.getSandboxMerchantConfig(param);
        return resultMono.map(Result::parse).map(e -> {
            redisService.set(cacheKey, JSONUtil.toJsonStr(e), 60); //秒
            return e;
        });
    }


    /**
     * 取生产环境 商户配置
     */
    public Mono<MerchantConfigDTO> getMerchantConfigDTO(String merchantId) {
        String cacheKey = GatewayConstant.CACHE_MERCHANT_CONFIG + merchantId;

        // 缓存获取
        Object obj = redisService.get(cacheKey);
        MerchantConfigDTO configDTO = Optional.ofNullable(obj).map(Object::toString)
                .map(e -> JSONUtil.toBean(e, MerchantConfigDTO.class))
                .orElse(null);
        if (Objects.nonNull(configDTO)) {
            return Mono.just(configDTO);
        }

        // 微服务获取
        MerchantIdParam param = new MerchantIdParam();
        param.setMerchantId(merchantId);
        Mono<Result<MerchantConfigDTO>> resultMono = merchantService.getMerchantConfig(param);
        return resultMono.map(Result::parse).map(e -> {
            redisService.set(cacheKey, JSONUtil.toJsonStr(e), 60);
            return e;
        });
    }

}
