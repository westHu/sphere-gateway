package com.sphere.config.property;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;

@Configuration
public class ReactNettyConfiguration {

    @Value("${reactor.netty.select-count:1}")
    String selectCount;

    @Value("${reactor.netty.worker-count:64}")
    String workerCount;

    @Bean
    public ReactorResourceFactory reactorClientResourceFactory() {
        System.setProperty("reactor.netty.pool.leasingStrategy", "lifo");
        System.setProperty("reactor.netty.ioSelectCount", selectCount);
        System.setProperty("reactor.netty.ioWorkerCount", workerCount);
        return new ReactorResourceFactory();
    }

}