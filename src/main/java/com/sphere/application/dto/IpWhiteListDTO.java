package com.sphere.application.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IP白名单数据传输对象
 * 用于封装允许访问的IP地址列表
 * 用于API访问控制和安全管理
 *
 * @author sphere
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class IpWhiteListDTO {

    /**
     * IP地址列表
     * 多个IP地址用逗号分隔
     * 例如: "192.168.1.1,192.168.1.2"
     * 为空表示不限制IP访问
     */
    private String ipList;

}
