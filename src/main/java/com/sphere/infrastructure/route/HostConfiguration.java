package com.sphere.infrastructure.route;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.host")
@RefreshScope
public class HostConfiguration {

    /**
     * 沙箱
     */
    private String sandbox = "sandbox-gateway.paysphere.id";

    /**
     * 非沙箱
     */
    private String product = "gateway.paysphere.id";

    /**
     * 是否开启
     */
    private String open = "true";
}
