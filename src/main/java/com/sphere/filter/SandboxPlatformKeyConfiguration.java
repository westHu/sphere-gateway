package com.sphere.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "pay.platform.sandbox")
public class SandboxPlatformKeyConfiguration {

    /**
     * 证书编码
     */
    private String key;

    /**
     * 证书密码
     */
    private String gateway;


    /**
     * 沙箱从配置文件解析私钥
     */
    public String parseSandboxPrivateKey() throws Exception {
        byte[] decode = Base64.getDecoder().decode(this.getKey().getBytes());
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new ByteArrayInputStream(decode), null);

        String certAlias = this.getGateway().substring(8, 24);
        String certPassword = this.getGateway().substring(24, 40);

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(certAlias, certPassword.toCharArray());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        byte[] encoded = spec.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

}

