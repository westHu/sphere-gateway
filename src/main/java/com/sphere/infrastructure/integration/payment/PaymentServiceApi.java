package com.sphere.infrastructure.integration.payment;

import com.sphere.infrastructure.integration.payment.dto.MerchantConfigDTO;
import com.sphere.infrastructure.integration.payment.dto.MerchantSandboxConfigDTO;
import com.sphere.infrastructure.integration.payment.param.MerchantIdParam;
import com.sphere.api.vo.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

/**
 * 支付服务API接口
 * 使用@HttpExchange注解定义HTTP接口
 * 支持动态服务地址配置
 *
 * @author sphere
 * @since 1.0.0
 */
@HttpExchange
public interface PaymentServiceApi {

    /**
     * 查询沙箱商户配置
     *
     * @param param 商户ID参数
     * @return 沙箱商户配置信息
     */
    @PostExchange("/sandbox/v1/getMerchantConfig")
    Mono<Result<MerchantSandboxConfigDTO>> getSandboxMerchantConfig(@RequestBody MerchantIdParam param);

    /**
     * 查询商户配置
     *
     * @param param 商户ID参数
     * @return 商户配置信息
     */
    @PostExchange("/v1/getMerchantConfig")
    Mono<Result<MerchantConfigDTO>> getMerchantConfig(@RequestBody MerchantIdParam param);
}
