package com.sphere.result;

import cn.hutool.json.JSONUtil;
import com.sphere.GatewayConstant;
import com.sphere.exception.GatewayException;
import com.sphere.exception.GatewayExceptionCode;
import com.sphere.utils.ValidationUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;


@Slf4j
@Data
public class BaseResult {

    private Integer code;

    private String message;

    private String traceId;


    /**
     * for http parse
     */
    public static <T> T parse(Result<T> result) {
        return parse(result, false);
    }

    public static <T> T parse(Result<T> result, boolean validate) {
        log.info("gateway post result={}", JSONUtil.toJsonStr(result));

        if (Objects.isNull(result)) {
            log.error("Post error: result is null");
            throw new GatewayException(GatewayExceptionCode.SERVER_ERROR, "Result is null");

        }

        if (!result.getCode().equals(GatewayConstant.SUCCESS)) {
            log.error("Post error. code not equal {} ? error message:{}", result.getCode(), result.getMessage());
            throw new GatewayException(GatewayExceptionCode.SERVER_ERROR, result.getMessage());
        }

        if (Objects.isNull(result.getData())) {
            return null;
        }

        T t = result.getData();
        if (validate) {
            String errorMsg = ValidationUtil.getErrorMsg(t);
            if (StringUtils.isNotBlank(errorMsg)) {
                throw new GatewayException(GatewayExceptionCode.SERVER_ERROR, errorMsg);
            }
        }
        return t;
    }
}


