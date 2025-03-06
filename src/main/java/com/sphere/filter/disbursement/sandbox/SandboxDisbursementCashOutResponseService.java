package com.sphere.filter.disbursement.sandbox;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sphere.enums.ServiceCodeEnum;
import com.sphere.exception.GatewayException;
import com.sphere.filter.AbstractResponseService;
import com.sphere.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;

import static com.sphere.exception.GatewayExceptionCode.BAD_REQUEST;

@Slf4j
@Component
public class SandboxDisbursementCashOutResponseService extends AbstractResponseService {


    /**
     * 处理 response
     */
    public String handlerResponse(ServerWebExchange serverWebExchange, String raw) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        String path = RequestUtil.getPath(request);

        ServiceCodeEnum serviceCodeEnum = ServiceCodeEnum.pathToEnum(path);
        if (Objects.isNull(serviceCodeEnum)) {
            throw new GatewayException(ServiceCodeEnum.UNKNOWN, BAD_REQUEST);
        }

        // 处理结果
        String data = handlerResult(serviceCodeEnum, raw, "SandboxDisbursementCashOutResponseService");

        JSONObject object = buildSuccessHeader(data);
        return JSONUtil.toJsonStr(object);
    }

}
