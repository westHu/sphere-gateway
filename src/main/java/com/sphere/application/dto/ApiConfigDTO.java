package com.sphere.application.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * API配置数据传输对象
 * 用于封装商户API相关的配置信息
 * 包括密钥、白名单等安全配置
 *
 * @author sphere
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class ApiConfigDTO {

    /**
     * 商户密钥
     * 用于API请求签名验证
     * 需要妥善保管，不能泄露
     */
    private String merchantSecret;

    /**
     * 商户公钥
     * 用于数据加密和验证
     * 可以分发给客户端
     */
    private String publicKey;

    /**
     * 公钥有效期
     * 超过此时间公钥将失效
     * 需要定期更新
     */
    private LocalDateTime expiryDate;

    /**
     * IP白名单
     * 允许访问的IP地址列表
     * 多个IP用逗号分隔
     * 为空表示不限制IP
     */
    private String ipWhiteList;
}
