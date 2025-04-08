package com.sphere.infrastructure.integration.payment.dto;

import lombok.Data;

@Data
public class SandboxMerchantConfigDTO {

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 商户秘钥
     */
    private String merchantSecret;

    /**
     * 商户收款完成回调地址
     */
    private String finishPaymentUrl;

    /**
     * 商户出款完成回调地址
     */
    private String finishCashUrl;

    /**
     * 商户退款完成回调地址
     */
    private String finishRefundUrl;

    /**
     * 商户支付完成跳转地址
     */
    private String finishRedirectUrl;

    /**
     * 商户公钥
     */
    private String publicKey;

    /**
     * 商户ip白名单
     */
    private String ipWhiteList;

    /**
     * 版本
     */
    private Integer version;

    /**
     * 扩展信息
     */
    private String attribute;

}

