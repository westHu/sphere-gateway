package com.sphere.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    /**
     * 测试API
     */
    @GetMapping(value = "/hi")
    public String hello() {
        return "PaySphere gateway";
    }

}
