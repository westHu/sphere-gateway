package com.sphere.filter.disbursement.sandbox;

import com.sphere.filter.AbstractRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SandboxDisbursementCashOutRequestService extends AbstractRequestService {

    /**
     * 处理 request
     */
    public Mono<String> handlerRequest(ServerWebExchange serverWebExchange, String raw) {
        //校验
        return verifyParameterThenSignature(serverWebExchange, raw, "SandboxDisbursementCashOutRequestService");
    }

}
