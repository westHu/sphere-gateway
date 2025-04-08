package com.sphere.common.utils;

import com.sphere.common.exception.GatewayException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据校验工具类
 * 用于对对象进行数据校验，支持JSR-303注解校验
 * 提供校验结果和错误信息的格式化输出
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
public class ValidationUtil {

    /**
     * 校验器工厂
     */
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();

    /**
     * 校验器实例
     */
    private static final Validator VALIDATOR = FACTORY.getValidator();

    /**
     * 私有构造函数，防止实例化
     */
    private ValidationUtil() {
        throw new GatewayException("Utility classes should not have public constructors");
    }

    /**
     * 校验对象
     * 使用JSR-303注解进行数据校验
     *
     * @param object 待校验的对象
     * @param <T> 对象的类型
     * @return 校验结果集合，如果为空则表示校验通过
     */
    public static <T> Set<ConstraintViolation<T>> validate(T object) {
        if (object == null) {
            log.warn("校验对象为空");
            return Set.of();
        }
        return VALIDATOR.validate(object);
    }

    /**
     * 获取校验错误信息
     * 将校验结果格式化为易读的字符串
     *
     * @param object 待校验的对象
     * @param <T> 对象的类型
     * @return 格式化的错误信息，如果校验通过则返回null
     */
    public static <T> String getErrorMsg(T object) {
        Set<ConstraintViolation<T>> violations = validate(object);
        if (CollectionUtils.isEmpty(violations)) {
            return null;
        }
        return violations.stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining(", "));
    }
}