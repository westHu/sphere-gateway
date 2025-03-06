package com.sphere.filter.payment;

import com.sphere.filter.AbstractRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import static com.sphere.GatewayConstant.REQUEST_PARAM;

@Slf4j
@Component
public class PaymentRequestService extends AbstractRequestService {

    /**
     * 处理 request
     */
    public String handlerRequest(ServerWebExchange serverWebExchange, String raw) {
        //参数设置到Attribute
        serverWebExchange.getAttributes().put(REQUEST_PARAM, raw);
        return raw;
    }

}
