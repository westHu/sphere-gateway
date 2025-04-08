package com.sphere.infrastructure.filter.transaction.production;

import com.sphere.common.enums.ServiceCodeEnum;;
import com.sphere.common.exception.GatewayException;
import com.sphere.infrastructure.filter.AbstractRequestService;
import com.sphere.infrastructure.integration.payment.dto.MerchantConfigDTO;
import com.sphere.common.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.sphere.common.constants.GatewayConstant.MERCHANT_CONFIG;
import static com.sphere.common.exception.GatewayExceptionCode.UNAUTHORIZED;

@Slf4j
@Component
public class TransactionPayInRequestService extends AbstractRequestService {

    /**
     * 处理 request
     */
    public Mono<String> handlerRequest(ServerWebExchange serverWebExchange, String raw) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        String partnerId = RequestUtil.getPartnerId(request);

        String responseMessage = "Unknown Client. Merchant ID: " + partnerId;
        MerchantConfigDTO configDTO = serverWebExchange.getAttribute(MERCHANT_CONFIG);
        if (Objects.isNull(configDTO)) {
            log.error("handlerRequest Merchant config not exist. merchantId={}", partnerId);
            return Mono.error(new GatewayException(ServiceCodeEnum.TRANSACTION_DEPOSIT, UNAUTHORIZED, responseMessage));
        }

        //校验
        return verifyParameterThenSignature(serverWebExchange, raw, "TransactionPayInRequestService");
    }

}
