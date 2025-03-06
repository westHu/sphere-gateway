package com.sphere.remote.payment;


import com.sphere.remote.payment.dto.MerchantConfigDTO;
import com.sphere.remote.payment.dto.SandboxMerchantConfigDTO;
import com.sphere.remote.payment.param.MerchantIdParam;
import com.sphere.result.Result;
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
