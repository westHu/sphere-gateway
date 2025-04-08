package com.sphere.infrastructure.integration.payment;


import com.sphere.infrastructure.integration.payment.dto.MerchantConfigDTO;
import com.sphere.infrastructure.integration.payment.dto.SandboxMerchantConfigDTO;
import com.sphere.infrastructure.integration.payment.param.MerchantIdParam;
import com.sphere.api.vo.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface PaymentServiceApi {

    /**
     * 沙箱 查询沙箱商户配置
     */
    @PostExchange("/sandbox/v1/getMerchantConfig")
    Mono<Result<SandboxMerchantConfigDTO>> getSandboxMerchantConfig(@RequestBody MerchantIdParam param);

    /**
     * 查询商户配置
     */
    @PostExchange("/v1/getMerchantConfig")
    Mono<Result<MerchantConfigDTO>> getMerchantConfig(@RequestBody MerchantIdParam param);
}
