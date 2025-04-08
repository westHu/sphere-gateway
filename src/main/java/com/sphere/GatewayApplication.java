package com.sphere;

import com.sphere.common.constants.GatewayConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.time.LocalDateTime;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication implements CommandLineRunner {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(GatewayConstant.ZONE_ID));
        SpringApplication.run(GatewayApplication.class, args);
    }


    @Override
    public void run(String... args) {
        log.info("===> Congratulations paysphere pay!");
        log.info("===> paysphere gateway started success!!!  Time:{}", LocalDateTime.now());
    }
}
