package com.sphere.common.constants;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 网关常量类
 * 定义网关服务中使用的各种常量，包括：
 * 1. HTTP相关常量（状态码、请求头等）
 * 2. 缓存相关常量
 * 3. 时间相关常量
 * 4. 环境相关常量
 * 5. 服务URL常量
 *
 * @author sphere
 * @since 1.0.0
 */
public class GatewayConstant {

    /**
     * ================ HTTP相关常量 ================
     */
    
    /**
     * HTTP成功状态码
     */
    public static final Integer SUCCESS = 200;

    /**
     * 响应字段名 - 状态码
     */
    public static final String CODE = "code";

    /**
     * 响应字段名 - 消息
     */
    public static final String MESSAGE = "message";

    /**
     * 响应字段名 - 数据
     */
    public static final String DATA = "data";

    /**
     * 请求头 - Bearer认证
     */
    public static final String BEARER = "Bearer";

    /**
     * 请求头 - 内容类型
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * 请求头 - 授权
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * 请求头 - 时间戳
     */
    public static final String X_TIMESTAMP = "X-TIMESTAMP";

    /**
     * 请求头 - 签名
     */
    public static final String X_SIGNATURE = "X-SIGNATURE";

    /**
     * 请求头 - 来源
     */
    public static final String ORIGIN = "ORIGIN";

    /**
     * 请求头 - 合作伙伴ID
     */
    public static final String X_PARTNER_ID = "X-PARTNER-ID";

    /**
     * 请求头 - 主机名
     */
    public static final String HOST_HEADER_NAME = "Host";

    /**
     * 请求头 - IP地址
     */
    public static final String IP_HEADER_NAME = "CF-Connecting-IP";

    /**
     * 请求头 - 来源域名
     */
    public static final String HEADER_ORIGIN = "http://paysphere.id";

    /**
     * ================ 缓存相关常量 ================
     */

    /**
     * 商户ID缓存键前缀
     */
    public static final String MERCHANT_ID = "MERCHANT_ID";

    /**
     * 商户配置缓存键前缀
     */
    public static final String MERCHANT_CONFIG = "MERCHANT_CONFIG";

    /**
     * 请求参数缓存键前缀
     */
    public static final String REQUEST_PARAM = "REQUEST_PARAM";

    /**
     * 生产环境商户配置缓存键前缀
     */
    public static final String CACHE_MERCHANT_CONFIG = "CACHE_MERCHANT_CONFIG:";

    /**
     * 沙箱环境商户配置缓存键前缀
     */
    public static final String SANDBOX_CACHE_MERCHANT_CONFIG = "SANDBOX_CACHE_MERCHANT_CONFIG:";

    /**
     * ================ 时间相关常量 ================
     */

    /**
     * 默认日期时间格式
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    /**
     * 默认日期时间格式化器
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    /**
     * 默认时区 - 北京
     */
    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * ================ 环境相关常量 ================
     */

    /**
     * 沙箱环境标识
     */
    public static final String SANDBOX = "sandbox";

    /**
     * ================ 服务URL常量 ================
     */

    /**
     * 支付服务URL
     */
    public static final String URL_PAYMENT = "lb://sphere-payment";
}
