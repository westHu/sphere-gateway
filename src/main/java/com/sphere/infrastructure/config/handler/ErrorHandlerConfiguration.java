package com.sphere.infrastructure.config.handler;

import lombok.Data;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.List;

/**
 * 错误处理器配置类
 * 配置全局异常处理器，用于统一处理网关中的各种异常
 * 包括：
 * 1. 配置自定义异常处理器（CustomWebExceptionHandler）
 * 2. 设置异常处理器的优先级为最高
 * 3. 配置视图解析器和消息编解码器
 *
 * @author sphere
 * @since 1.0.0
 */
@Data
@Configuration
@EnableConfigurationProperties({ServerProperties.class})
public class ErrorHandlerConfiguration {

    /**
     * 服务器配置属性
     */
    private final ServerProperties serverProperties;

    /**
     * Spring应用上下文
     */
    private final ApplicationContext applicationContext;
    
    /**
     * 视图解析器列表
     */
    private final List<ViewResolver> viewResolvers;

    /**
     * 服务器编解码器配置
     */
    private final ServerCodecConfigurer serverCodecConfigurer;

    /**
     * 配置全局异常处理器
     * 创建一个自定义的Web异常处理器，并设置相关配置
     *
     * @param errorAttributes 错误属性
     * @return 配置好的异常处理器
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes) {
        // 创建自定义异常处理器
        CustomWebExceptionHandler exceptionHandler = new CustomWebExceptionHandler(
                errorAttributes,
                this.serverProperties.getError(),
                this.applicationContext);
        
        // 配置视图解析器
        exceptionHandler.setViewResolvers(this.viewResolvers);
        
        // 配置消息编解码器
        exceptionHandler.setMessageWriters(this.serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(this.serverCodecConfigurer.getReaders());
        
        return exceptionHandler;
    }
}
