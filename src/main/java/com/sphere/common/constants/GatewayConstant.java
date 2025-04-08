package com.sphere.common.constants;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class GatewayConstant {

    public static final Integer SUCCESS = 200;
    public static final String SANDBOX = "sandbox";

    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String DATA = "data";

    public static final String MERCHANT_ID = "MERCHANT_ID";
    public static final String MERCHANT_CONFIG = "MERCHANT_CONFIG";
    public static final String REQUEST_PARAM = "REQUEST_PARAM";
    public static final String HEADER_ORIGIN = "http://paysphere.id";


    public static final String F_1 = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final DateTimeFormatter DF_1 = DateTimeFormatter.ofPattern(F_1);

    /**
     * sign
     */
    public static final String BEARER = "Bearer";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String X_TIMESTAMP = "X-TIMESTAMP";
    public static final String X_SIGNATURE = "X-SIGNATURE";
    public static final String ORIGIN = "ORIGIN";
    public static final String X_PARTNER_ID = "X-PARTNER-ID";
    public static final String HOST_HEADER_NAME = "Host";
    public static final String IP_HEADER_NAME = "CF-Connecting-IP";

    /**
     * zoned
     */
    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Beijing");


    public static final String CACHE_MERCHANT_CONFIG = "CACHE_MERCHANT_CONFIG:";
    public static final String SANDBOX_CACHE_MERCHANT_CONFIG = "SANDBOX_CACHE_MERCHANT_CONFIG:";

    /**
     * URL
     */
    public static final String URL_PAYMENT = "lb://sphere-payment";

}
