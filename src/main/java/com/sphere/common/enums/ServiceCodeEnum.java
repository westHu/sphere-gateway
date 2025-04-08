package com.sphere.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 服务代码枚举
 * 定义系统中所有服务的代码和对应的路径映射
 * 包括生产环境和沙箱环境的路径配置
 *
 * @author sphere
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ServiceCodeEnum {

    /**
     * 交易存款服务
     * 用于处理商户的存款交易请求
     */
    TRANSACTION_DEPOSIT("10",
            "/v1.0/transaction/deposit",    // 生产环境路径
            "/sandbox/v1.0/deposit",        // 沙箱环境路径
            "/v1.0/deposit"),               // 重写路径

    /**
     *  disbursement payout服务
     * 用于处理商户的提现请求
     */
    DISBURSEMENT_PAYOUT("11",
            "/v1.0/disbursement/payout",    // 生产环境路径
            "/sandbox/v1.0/payout",         // 沙箱环境路径
            "/v1.0/payout"),                // 重写路径

    /**
     * 状态查询服务
     * 用于查询交易状态
     */
    INQUIRY_STATUS("12",
            "/v1.0/inquiry-status",         // 生产环境路径
            "/sandbox/v1/inquiryStatus",    // 沙箱环境路径
            "/v1/inquiryStatus"),           // 重写路径

    /**
     * 余额查询服务
     * 用于查询账户余额
     */
    INQUIRY_BALANCE("13",
            "/v1.0/inquiry-balance",        // 生产环境路径
            "/sandbox/v1.0/inquiryBalance", // 沙箱环境路径
            "/v1.0/inquiryBalance"),        // 重写路径

    /**
     * 未知服务
     * 用于处理未定义的服务请求
     */
    UNKNOWN("00", 
            "/v1.0/unknown",                // 生产环境路径
            "sandbox/unknown",              // 沙箱环境路径
            "/unknown");                    // 重写路径

    /**
     * 服务代码
     * 用于标识不同的服务类型
     */
    private final String serviceCode;

    /**
     * 服务路径
     * 生产环境下的API路径
     */
    private final String path;

    /**
     * 沙箱环境重写路径
     * 沙箱环境下的API路径
     */
    private final String sandboxRewritePath;

    /**
     * 重写路径
     * 用于路径重写的目标路径
     */
    private final String rewritePath;

    /**
     * 根据路径获取对应的服务代码枚举
     * 通过匹配路径中的关键字来确定服务类型
     *
     * @param path 请求路径
     * @return 对应的服务代码枚举，如果未找到则返回null
     */
    public static ServiceCodeEnum pathToEnum(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        return Arrays.stream(ServiceCodeEnum.values())
                .filter(e -> path.contains(e.getPath()))
                .findAny()
                .orElse(null);
    }
}
