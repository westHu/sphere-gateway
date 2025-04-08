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

/**
 * 抽象响应服务类
 * 提供响应处理的基础功能，包括响应数据验证、错误处理等
 * 
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractResponseService {

    /**
     * 处理业务返回的结果
     * 包括以下验证：
     * 1. 响应数据非空验证
     * 2. 响应码验证
     * 3. 响应数据格式验证
     *
     * @param serviceCodeEnum 服务类型枚举
     * @param raw 原始响应数据
     * @param method 方法名称（用于日志）
     * @return 处理后的响应数据
     * @throws GatewayException 当响应数据验证失败时抛出
     */
    protected String handlerResult(ServiceCodeEnum serviceCodeEnum, String raw, String method) {
        // 记录原始响应数据
        log.info("{} 开始处理响应数据: {}", method, raw);
        
        // 验证响应数据非空
        if (StringUtils.isBlank(raw)) {
            log.error("{} 响应数据为空", method);
            throw new GatewayException(serviceCodeEnum, SERVER_ERROR, "响应数据为空");
        }

        // 解析响应数据
        JSONObject jsonObject = JSONUtil.parseObj(raw);
        
        // 验证响应码
        Integer code = jsonObject.getInt(GatewayConstant.CODE);
        if (Objects.isNull(code)) {
            log.error("{} 响应码为空, 原始数据: {}", method, raw);
            throw new GatewayException(serviceCodeEnum, SERVER_ERROR, "响应码为空");
        }

        // 验证响应码是否为成功码
        if (!code.equals(200)) {
            String message = jsonObject.getStr(GatewayConstant.MESSAGE);
            log.error("{} 响应码错误: {}, 错误信息: {}", method, code, message);
            throw new GatewayException(serviceCodeEnum, SERVER_ERROR, message);
        }

        // 验证响应数据
        String data = jsonObject.getStr(GatewayConstant.DATA);
        if (StringUtils.isBlank(data)) {
            log.error("{} 响应数据为空", method);
            throw new GatewayException(serviceCodeEnum, SERVER_ERROR, "响应数据为空");
        }

        log.info("{} 响应数据处理完成", method);
        return data;
    }

    /**
     * 构建成功响应头
     * 如果响应数据中缺少必要的字段，则添加默认值
     *
     * @param data 响应数据
     * @return 包含成功响应头的JSONObject
     */
    protected JSONObject buildSuccessHeader(String data) {
        JSONObject object = JSONUtil.parseObj(data);
        GatewayExceptionCode exceptionCode = GatewayExceptionCode.SUCCESS;

        // 设置默认响应码和消息
        object.putIfAbsent(GatewayConstant.CODE, exceptionCode.getCode());
        object.putIfAbsent(GatewayConstant.MESSAGE, exceptionCode.getMessage());
        
        return object;
    }
}
