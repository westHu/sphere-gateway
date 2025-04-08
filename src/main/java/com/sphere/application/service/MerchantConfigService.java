package com.sphere.application.service;

import cn.hutool.json.JSONUtil;
import com.sphere.application.dto.ApiConfigDTO;
import com.sphere.common.constants.GatewayConstant;
import com.sphere.infrastructure.cache.LocalCacheService;
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

/**
 * 商户配置服务
 * 负责管理商户的配置信息，包括生产环境和沙箱环境
 * 提供配置的缓存管理和远程获取功能
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@Component
public class MerchantConfigService {

    /**
     * 缓存过期时间（秒）
     */
    private static final int CACHE_EXPIRE_SECONDS = 60;

    /**
     * 缓存名称
     */
    private static final String CACHE_NAME = "merchant_config";

    @Resource
    PaymentServiceApi paymentServiceApi;

    @Resource
    LocalCacheService localCacheService;

    /**
     * 获取商户配置信息
     * 根据环境（生产/沙箱）获取对应的商户配置
     * 包含公钥、密钥、IP白名单等信息
     *
     * @param merchantId 商户ID
     * @param hostName 主机名，用于判断环境
     * @return 商户配置信息
     */
    public Mono<ApiConfigDTO> getApiConfigDTO(String merchantId, String hostName) {
        log.debug("开始获取商户配置 - 商户ID: {}, 主机名: {}", merchantId, hostName);
        boolean isSandbox = hostName.contains(GatewayConstant.SANDBOX);

        return isSandbox ? 
            getSandboxMerchantConfigDTO(merchantId).map(this::convertToApiConfigDTO) :
            getMerchantConfigDTO(merchantId).map(this::convertToApiConfigDTO);
    }

    /**
     * 获取沙箱环境商户配置
     * 优先从缓存获取，缓存不存在则从远程服务获取
     *
     * @param merchantId 商户ID
     * @return 沙箱环境商户配置
     */
    public Mono<SandboxMerchantConfigDTO> getSandboxMerchantConfigDTO(String merchantId) {
        String cacheKey = GatewayConstant.SANDBOX_CACHE_MERCHANT_CONFIG + merchantId;
        log.debug("获取沙箱商户配置 - 商户ID: {}, 缓存键: {}", merchantId, cacheKey);

        // 从缓存获取
        return getFromCache(cacheKey, SandboxMerchantConfigDTO.class)
            .switchIfEmpty(getSandboxConfigFromRemote(merchantId));
    }

    /**
     * 获取生产环境商户配置
     * 优先从缓存获取，缓存不存在则从远程服务获取
     *
     * @param merchantId 商户ID
     * @return 生产环境商户配置
     */
    public Mono<MerchantConfigDTO> getMerchantConfigDTO(String merchantId) {
        String cacheKey = GatewayConstant.CACHE_MERCHANT_CONFIG + merchantId;
        log.debug("获取生产环境商户配置 - 商户ID: {}, 缓存键: {}", merchantId, cacheKey);

        // 从缓存获取
        return getFromCache(cacheKey, MerchantConfigDTO.class)
            .switchIfEmpty(getProductionConfigFromRemote(merchantId));
    }

    /**
     * 从缓存获取配置
     *
     * @param cacheKey 缓存键
     * @param clazz 目标类型
     * @return 配置信息
     */
    private <T> Mono<T> getFromCache(String cacheKey, Class<T> clazz) {
        Object obj = localCacheService.get(CACHE_NAME, cacheKey);
        T configDTO = Optional.ofNullable(obj)
            .map(Object::toString)
            .map(e -> JSONUtil.toBean(e, clazz))
            .orElse(null);

        if (Objects.nonNull(configDTO)) {
            log.debug("从缓存获取配置成功 - 缓存键: {}", cacheKey);
            return Mono.just(configDTO);
        }
        return Mono.empty();
    }

    /**
     * 从远程服务获取沙箱环境配置
     *
     * @param merchantId 商户ID
     * @return 沙箱环境配置
     */
    private Mono<SandboxMerchantConfigDTO> getSandboxConfigFromRemote(String merchantId) {
        MerchantIdParam param = new MerchantIdParam();
        param.setMerchantId(merchantId);

        return paymentServiceApi.getSandboxMerchantConfig(param)
            .map(Result::parse)
            .map(config -> {
                String cacheKey = GatewayConstant.SANDBOX_CACHE_MERCHANT_CONFIG + merchantId;
                localCacheService.put(CACHE_NAME, cacheKey, JSONUtil.toJsonStr(config));
                log.debug("从远程服务获取沙箱配置成功并更新缓存 - 商户ID: {}", merchantId);
                return config;
            });
    }

    /**
     * 从远程服务获取生产环境配置
     *
     * @param merchantId 商户ID
     * @return 生产环境配置
     */
    private Mono<MerchantConfigDTO> getProductionConfigFromRemote(String merchantId) {
        MerchantIdParam param = new MerchantIdParam();
        param.setMerchantId(merchantId);

        return paymentServiceApi.getMerchantConfig(param)
            .map(Result::parse)
            .map(config -> {
                String cacheKey = GatewayConstant.CACHE_MERCHANT_CONFIG + merchantId;
                localCacheService.put(CACHE_NAME, cacheKey, JSONUtil.toJsonStr(config));
                log.debug("从远程服务获取生产环境配置成功并更新缓存 - 商户ID: {}", merchantId);
                return config;
            });
    }

    /**
     * 将商户配置转换为API配置
     *
     * @param config 商户配置
     * @return API配置
     */
    private ApiConfigDTO convertToApiConfigDTO(MerchantConfigDTO config) {
        ApiConfigDTO apiConfigDTO = new ApiConfigDTO();
        apiConfigDTO.setPublicKey(config.getPublicKey());
        apiConfigDTO.setMerchantSecret(config.getMerchantSecret());
        apiConfigDTO.setExpiryDate(config.getExpiryDate());
        apiConfigDTO.setIpWhiteList(config.getIpWhiteList());
        return apiConfigDTO;
    }

    /**
     * 将沙箱商户配置转换为API配置
     *
     * @param config 沙箱商户配置
     * @return API配置
     */
    private ApiConfigDTO convertToApiConfigDTO(SandboxMerchantConfigDTO config) {
        ApiConfigDTO apiConfigDTO = new ApiConfigDTO();
        apiConfigDTO.setPublicKey(config.getPublicKey());
        apiConfigDTO.setMerchantSecret(config.getMerchantSecret());
        apiConfigDTO.setIpWhiteList(config.getIpWhiteList());
        return apiConfigDTO;
    }
}
