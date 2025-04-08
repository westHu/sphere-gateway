package com.sphere;

import com.sphere.common.constants.GatewayConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.TimeZone;

import static com.sphere.common.constants.GatewayConstant.ZONE_ID;

/**
 * PaySphere Gateway 启动类
 *
 * @author paysphere
 */
@Slf4j
@SpringBootApplication
@EnableWebFlux
public class GatewayApplication implements CommandLineRunner {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZONE_ID));
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("===> Congratulations paysphere!");
        log.info("===> paysphere gateway started success!!!  Time:{}", LocalDateTime.now());
    }

    /**
     * 配置负载均衡的WebClient
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
