package com.sphere.api.vo;

import com.sphere.common.constants.GatewayConstant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用响应结果
 * 用于封装单个对象的响应数据，继承自BaseResult
 * 提供数据对象的包装和解析功能
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class Result<T> extends BaseResult {

    /**
     * 响应数据
     * 泛型T表示具体的数据类型
     * 可以是任意Java对象
     */
    private T data;

    /**
     * 创建成功响应
     * 使用默认成功消息
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success(T data) {
        log.debug("创建响应 - 数据类型: {}", data != null ? data.getClass().getSimpleName() : "null");
        Result<T> result = new Result<>();
        result.setCode(GatewayConstant.SUCCESS);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 创建成功响应
     * 使用自定义成功消息
     *
     * @param data 响应数据
     * @param message 自定义成功消息
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success(T data, String message) {
        log.debug("创建响应 - 数据类型: {}, 消息: {}", 
            data != null ? data.getClass().getSimpleName() : "null", message);
        Result<T> result = new Result<>();
        result.setCode(GatewayConstant.SUCCESS);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}

