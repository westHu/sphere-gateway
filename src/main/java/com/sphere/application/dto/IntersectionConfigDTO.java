package com.sphere.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IntersectionConfigDTO {

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 商户秘钥
     */
    private String merchantSecret;

    /**
     * 商户公钥
     */
    private String publicKey;

    /**
     * 公钥有效期
     */
    private LocalDateTime expiryDate;

    /**
     * 商户ip白名单
     */
    private String ipWhiteList;

}
