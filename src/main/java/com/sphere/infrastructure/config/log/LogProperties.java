package com.sphere.infrastructure.config.log;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = LogProperties.PREFIX)
public class LogProperties {

    public static final String PREFIX = "gateway.config.log";

    private ApiAlarmConfiguration fail = new ApiAlarmConfiguration();

    private SlowApiAlarmConfiguration slow = new SlowApiAlarmConfiguration();

    /**
     * 慢API报警配置
     */
    @Data
    public static class SlowApiAlarmConfiguration {

        /**
         * 是否开启API慢日志打印
         */
        private boolean alarm = true;

        /**
         * 报警阈值 （单位：毫秒）
         */
        private long threshold = 5000;
    }


    /**
     * API异常报警(根据http状态码判定）
     */
    @Data
    public static class ApiAlarmConfiguration {

        /**
         * 是否开启异常报警
         */
        private boolean alarm = true;

        /**
         * 排除状态码
         */
        private List<Integer> exclusion;
    }
}
