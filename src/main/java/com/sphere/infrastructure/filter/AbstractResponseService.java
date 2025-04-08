package com.sphere.infrastructure.filter;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sphere.common.constants.GatewayConstant;
import com.sphere.common.enums.ServiceCodeEnum;
import com.sphere.common.exception.GatewayException;
import com.sphere.common.exception.GatewayExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import static com.sphere.common.exception.GatewayExceptionCode.SERVER_ERROR;

@Slf4j
public abstract class AbstractResponseService {

    /**
     * 处理业务返回的结果
     */
    protected String handlerResult(ServiceCodeEnum serviceCodeEnum, String raw, String method) {
        log.info("{} raw={}", method, raw);
        if (StringUtils.isBlank(raw)) {
            log.error("{} response is blank", method);
            throw new GatewayException(serviceCodeEnum, SERVER_ERROR);
        }

        //校验response code 如果不等于200，则业务异常
        JSONObject jsonObject = JSONUtil.parseObj(raw);
        Integer code = jsonObject.getInt(GatewayConstant.CODE);
        if (Objects.isNull(code)) {
            log.error("{} response raw error. {}", method, raw);
            throw new GatewayException(serviceCodeEnum, SERVER_ERROR, raw);
        }

        if (!code.equals(200)) {
            String message = jsonObject.getStr(GatewayConstant.MESSAGE);
            log.error("{} response code error. {}", method, message);
            throw new GatewayException(serviceCodeEnum, SERVER_ERROR, message);
        }

        //解析 response data
        String data = jsonObject.getStr(GatewayConstant.DATA);
        if (StringUtils.isBlank(data)) {
            log.error("{} response data is blank", method);
            throw new GatewayException(serviceCodeEnum, SERVER_ERROR);
        }

        return data;
    }


    protected JSONObject buildSuccessHeader(String data) {
        JSONObject object = JSONUtil.parseObj(data);
        GatewayExceptionCode exceptionCode = GatewayExceptionCode.SUCCESS;

        object.putIfAbsent(GatewayConstant.CODE, exceptionCode.getCode());
        object.putIfAbsent(GatewayConstant.MESSAGE, exceptionCode.getMessage());
        return object;
    }

}
