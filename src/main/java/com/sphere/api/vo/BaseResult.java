package com.sphere.api.vo;

import cn.hutool.json.JSONUtil;
import com.sphere.common.constants.GatewayConstant;
import com.sphere.common.exception.GatewayException;
import com.sphere.common.exception.GatewayExceptionCode;
import com.sphere.common.utils.ValidationUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 基础响应结果
 * 所有响应结果的基类，提供基础的响应字段和通用方法
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@Data
public class BaseResult {

    /**
     * 响应码
     * 200: 成功
     * 其他: 失败
     */
    private Integer code;

    /**
     * 响应消息
     * 成功时一般为"success"
     * 失败时为具体错误信息
     */
    private String message;

    /**
     * 追踪ID
     * 用于请求追踪和日志关联
     */
    private String traceId;

    /**
     * 解析HTTP响应结果
     * 不进行数据验证的简单解析
     *
     * @param result 响应结果
     * @param <T> 数据类型
     * @return 解析后的数据
     * @throws GatewayException 当响应为空或响应码不为成功时抛出
     */
    public static <T> T parse(Result<T> result) {
        return parse(result, false);
    }

    /**
     * 解析HTTP响应结果
     * 可选择是否进行数据验证
     *
     * @param result 响应结果
     * @param validate 是否验证数据
     * @param <T> 数据类型
     * @return 解析后的数据
     * @throws GatewayException 当响应为空、响应码不为成功或数据验证失败时抛出
     */
    public static <T> T parse(Result<T> result, boolean validate) {
        log.debug("开始解析响应结果: {}", JSONUtil.toJsonStr(result));

        if (Objects.isNull(result)) {
            log.error("响应结果为空");
            throw new GatewayException(GatewayExceptionCode.SERVER_ERROR, "Result is null");
        }

        if (!result.getCode().equals(GatewayConstant.SUCCESS)) {
            log.error("响应失败 - 响应码: {}, 错误信息: {}", result.getCode(), result.getMessage());
            throw new GatewayException(GatewayExceptionCode.SERVER_ERROR, result.getMessage());
        }

        if (Objects.isNull(result.getData())) {
            log.debug("响应数据为空");
            return null;
        }

        T data = result.getData();
        if (validate && StringUtils.isBlank(data.toString())) {
            log.error("数据验证失败 - 数据为空");
            throw new GatewayException(GatewayExceptionCode.SERVER_ERROR, "Data is empty");
        }

        log.debug("响应解析成功");
        return data;
    }
}


