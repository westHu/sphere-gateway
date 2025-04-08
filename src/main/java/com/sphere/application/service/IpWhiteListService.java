package com.sphere.application.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * IP白名单服务
 * 用于验证请求IP是否在白名单列表中
 * 提供IP访问控制功能
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@Component
public class IpWhiteListService {

    /**
     * 验证IP是否在白名单中
     * 如果白名单为空，则允许所有IP访问
     * 如果白名单不为空，则检查IP是否在白名单列表中
     *
     * @param ip 待验证的IP地址
     * @param ipWhiteList 白名单列表，多个IP用逗号分隔
     * @return true: IP在白名单中或白名单为空; false: IP不在白名单中
     */
    public boolean validateIpAccess(String ip, String ipWhiteList) {
        log.debug("开始验证IP - IP: {}, 白名单: {}", ip, ipWhiteList);

        // 如果白名单为空，允许所有IP访问
        if (StringUtils.isBlank(ipWhiteList)) {
            log.debug("白名单为空，允许所有IP访问");
            return true;
        }

        // 如果IP为空，拒绝访问
        if (StringUtils.isBlank(ip)) {
            log.warn("IP为空，拒绝访问");
            return false;
        }

        // 检查IP是否在白名单中
        boolean isAllowed = Arrays.stream(ipWhiteList.split(","))
                .map(String::trim)
                .anyMatch(whiteIp -> whiteIp.equals(ip));

        log.debug("IP验证结果 - IP: {}, 是否允许: {}", ip, isAllowed);
        return isAllowed;
    }
}
