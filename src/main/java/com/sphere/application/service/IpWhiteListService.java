package com.sphere.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Slf4j
@Component
public class IpWhiteListService {

    /**
     * 校验商户ip白名单
     */
    public boolean verifyIp(String ip, String ipWhite) {
        log.info("verifyIp ipWhite={} ip={}", ipWhite, ip);

        return Optional.ofNullable(ipWhite)
                .map(e -> e.contains(ip))
                .orElse(false);
    }
}
