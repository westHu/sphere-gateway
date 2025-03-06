package com.sphere.service.dto;

import lombok.Data;

@Data
public class IpWhiteListDTO {

    /**
     * 代收
     */
    private String pay;

    /**
     * 代付
     */
    private String cash;

    /**
     * 查询余额
     */
    private String balance;

}
