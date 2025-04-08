package com.sphere.infrastructure.filter.balance.sandbox;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sphere.common.enums.ServiceCodeEnum;;
import com.sphere.common.exception.GatewayException;
import com.sphere.common.exception.GatewayExceptionCode;
import com.sphere.infrastructure.filter.AbstractResponseService;
import com.sphere.common.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;

@Slf4j
@Component
public class SandboxBalanceResponseService extends AbstractResponseService {

    /**
     * 沙箱处理 response
     */
    public String handlerResponse(ServerWebExchange serverWebExchange, String raw) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        String path = RequestUtil.getPath(request);

        ServiceCodeEnum serviceCodeEnum = ServiceCodeEnum.pathToEnum(path);
        if (Objects.isNull(serviceCodeEnum)) {
            throw new GatewayException(ServiceCodeEnum.UNKNOWN, GatewayExceptionCode.BAD_REQUEST);
        }

        // 处理结果
        String data = handlerResult(serviceCodeEnum, raw, "SandboxBalanceResponseService");

        JSONObject object = buildSuccessHeader(data);
        return JSONUtil.toJsonStr(object);
    }

}
