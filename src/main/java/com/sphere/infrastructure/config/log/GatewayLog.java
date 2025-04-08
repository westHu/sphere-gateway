package com.sphere.infrastructure.config.log;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class GatewayLog implements Serializable {

    @Serial
    private static final long serialVersionUID = -3205904134722576668L;


    private String merchantId;


    //--执行之前数值-------
    /**
     * 访问实例
     */
    private String targetServer;

    /**
     * host
     */
    private String host;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 请求与方法
     */
    private String method;

    /**
     * 请求协议
     */
    private String schema;

    /**
     * 请求ip
     */
    private String ip;

    /**
     * 请求时间
     */
    private Date requestTime;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 请求参数
     */
    private String requestParam;


    //--执行之后数值-------

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 请求执行时间
     */
    private Long executeTime;

}
