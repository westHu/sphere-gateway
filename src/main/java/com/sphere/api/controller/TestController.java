package com.sphere.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 * 用于网关基础功能测试
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@RestController
public class TestController {

    /**
     * 测试网关连通性
     * 用于验证网关服务是否正常运行
     *
     * @return 返回网关服务标识
     */
    @GetMapping(value = "/hello")
    public String hello() {
        log.info("Gateway health check");
        return "PaySphere gateway";
    }

}
