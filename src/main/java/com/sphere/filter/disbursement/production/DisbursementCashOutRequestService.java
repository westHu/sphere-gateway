package com.sphere.filter.disbursement.production;

import cn.hutool.json.JSONUtil;
import com.sphere.enums.ServiceCodeEnum;
import com.sphere.exception.GatewayException;
import com.sphere.filter.AbstractRequestService;
import com.sphere.remote.payment.dto.MerchantConfigDTO;
import com.sphere.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.sphere.GatewayConstant.MERCHANT_CONFIG;
import static com.sphere.exception.GatewayExceptionCode.UNAUTHORIZED;

@Slf4j
@Component
public class DisbursementCashOutRequestService extends AbstractRequestService {

    /**
     * 处理 request
     */
    public Mono<String> handlerRequest(ServerWebExchange serverWebExchange, String raw) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        String path = RequestUtil.getPath(request);
        String partnerId = RequestUtil.getPartnerId(request);

        String responseMessage = "Unknown Client. Merchant ID: " + partnerId;
        MerchantConfigDTO configDTO = serverWebExchange.getAttribute(MERCHANT_CONFIG);
        if (Objects.isNull(configDTO)) {
            log.error("handlerRequest Merchant config not exist. merchantId={}", partnerId);
            return Mono.error(new GatewayException(ServiceCodeEnum.DISBURSEMENT_PAYOUT, UNAUTHORIZED, responseMessage));
        }
        Integer businessAction = Optional.of(configDTO).map(MerchantConfigDTO::getBusinessAction).orElse(3);

        //校验商户是否支持此接口
        responseMessage = "Api path not support access. Path: " + path;
        List<BusinessActionEnum> businessActionEnumList = BusinessActionEnum.businessActionEnumList(businessAction);
        log.info("handlerRequest businessActionEnumList={}", businessActionEnumList);
        if (!businessActionEnumList.contains(BusinessActionEnum.PAY_OUT)) {
            log.error("handlerRequest Merchant config not exist. param={}", JSONUtil.toJsonStr(businessActionEnumList));
            return Mono.error(new GatewayException(ServiceCodeEnum.DISBURSEMENT_PAYOUT, UNAUTHORIZED,
                    responseMessage));
        }

        //校验
        return verifyParameterThenSignature(serverWebExchange, raw, "DisbursementCashOutRequestService");
    }

}
