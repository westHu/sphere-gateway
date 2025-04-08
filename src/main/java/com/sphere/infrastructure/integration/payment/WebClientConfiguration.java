package com.sphere.infrastructure.integration.payment;

import com.sphere.common.constants.GatewayConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * 在此处添加备注信息
 */
@Configuration(proxyBeanMethods = false)
public class WebClientConfiguration {

    /**
     * merchant WebClient配置
     */
    @Bean
    public PaymentServiceApi merchantServiceApi(WebClient.Builder webClientBuilder) {
        WebClient webClient = webClientBuilder.baseUrl(GatewayConstant.URL_PAYMENT).build();
        return HttpServiceProxyFactory.builder().clientAdapter(WebClientAdapter.forClient(webClient)).build().createClient(PaymentServiceApi.class);
    }

}