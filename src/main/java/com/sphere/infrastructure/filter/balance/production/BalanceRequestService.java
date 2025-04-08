package com.sphere.infrastructure.filter.balance.production;

import com.sphere.infrastructure.filter.AbstractRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class BalanceRequestService extends AbstractRequestService {

    /**
     * 处理 request
     */
    public Mono<String> handlerRequest(ServerWebExchange serverWebExchange, String raw) {
        return verifyParameterThenSignature(serverWebExchange, raw, "BalanceRequestService");
    }

}
