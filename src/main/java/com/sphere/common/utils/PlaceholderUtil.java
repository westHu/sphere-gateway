package com.sphere.common.utils;

import com.sphere.common.exception.GatewayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Properties;

/**
 * 占位符替换工具类
 * 用于处理字符串中的占位符替换
 * 支持使用{1}, {2}等格式的占位符
 * 常用于消息模板的动态替换
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
public class PlaceholderUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private PlaceholderUtil() {
        throw new GatewayException("Utility classes should not have public constructors");
    }

    /**
     * 占位符前缀
     */
    private static final String PREFIX = "{";

    /**
     * 占位符后缀
     */
    private static final String SUFFIX = "}";

    /**
     * 占位符替换助手
     */
    private static final PropertyPlaceholderHelper PLACEHOLDER_HELPER = new PropertyPlaceholderHelper(PREFIX, SUFFIX);

    /**
     * 替换字符串中的占位符
     * 使用Properties中的值替换对应的占位符
     *
     * @param text 包含占位符的文本
     * @param properties 替换值集合
     * @return 替换后的文本
     */
    public static String replacePlaceholders(String text, Properties properties) {
        if (StringUtils.isBlank(text)) {
            log.warn("待替换文本为空");
            return text;
        }
        if (properties == null) {
            log.warn("替换值集合为空");
            return text;
        }
        return PLACEHOLDER_HELPER.replacePlaceholders(text, properties);
    }

    /**
     * 替换字符串中的占位符
     * 使用数组中的值按顺序替换占位符
     *
     * @param text 包含占位符的文本
     * @param values 替换值数组
     * @return 替换后的文本
     */
    public static String replacePlaceholders(String text, String... values) {
        if (StringUtils.isBlank(text)) {
            log.warn("待替换文本为空");
            return text;
        }
        if (values == null || values.length == 0) {
            log.warn("替换值数组为空");
            return text;
        }

        Properties properties = new Properties();
        for (int i = 0; i < values.length; i++) {
            properties.setProperty(String.valueOf(i + 1), values[i]);
        }
        return replacePlaceholders(text, properties);
    }
}

