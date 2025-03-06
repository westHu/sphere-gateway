package com.sphere.utils;

import com.sphere.exception.GatewayException;
import com.sphere.exception.GatewayExceptionCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Properties;

/*
 * 占位符替换工具类
 */

public class PlaceholderUtil {

    private PlaceholderUtil() {
        throw new GatewayException("Utility classes should not have public constructors");
    }

    private static final String PREFIX = "{";
    private static final String SUFFIX = "}";
    private static final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(PREFIX, SUFFIX);

    /**
     * 解析占位符
     */
    public static String resolve(String content, String... props) {
        if (StringUtils.isEmpty(content)) {
            return "";
        }
        if (null == props || props.length == 0) {
            return content;
        }
        Properties properties = new Properties();
        for (int i = 1; i <= props.length; i++) {
            properties.put(i + "", props[i - 1]);
        }
        return helper.replacePlaceholders(content, properties);
    }

}

