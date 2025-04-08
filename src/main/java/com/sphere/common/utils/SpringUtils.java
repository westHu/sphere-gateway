package com.sphere.common.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring上下文工具类
 * 用于在非Spring管理的类中获取Spring Bean
 * 实现ApplicationContextAware接口，在Spring启动时自动注入ApplicationContext
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@Component
public class SpringUtils implements ApplicationContextAware {
    
    /**
     * Spring应用上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * 获取Spring Bean实例
     * 根据类型获取对应的Bean对象
     *
     * @param tClass Bean的类型
     * @param <T> Bean的泛型类型
     * @return Bean实例
     * @throws BeansException 如果获取Bean失败
     */
    public static <T> T getBean(Class<T> tClass) {
        if (applicationContext == null) {
            log.error("ApplicationContext未初始化");
            throw new IllegalStateException("ApplicationContext未初始化");
        }
        return applicationContext.getBean(tClass);
    }

    /**
     * 设置ApplicationContext
     * 在Spring启动时自动调用此方法
     *
     * @param applicationContextParam Spring应用上下文
     * @throws BeansException 如果设置失败
     */
    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContextParam) throws BeansException {
        applicationContext = applicationContextParam;
        log.debug("ApplicationContext初始化成功");
    }
}


