package com.sphere.common.utils;

import com.sphere.common.exception.GatewayException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * 签名工具类
 * 提供多种签名算法的实现，包括：
 * 1. RSA签名（SHA256withRSA）
 * 2. HmacSHA512签名
 * 3. SHA256哈希
 * 支持签名生成和验证
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
public class SignUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * RSA密钥算法
     */
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * RSA签名算法
     */
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /**
     * 私有构造函数，防止实例化
     */
    private SignUtil() {
        throw new GatewayException("Utility classes should not have public constructors");
    }

    /**
     * RSA签名
     * 使用私钥对内容进行签名
     *
     * @param content 待签名内容
     * @param privateKeyStr Base64编码的私钥
     * @param encode 字符编码
     * @return Base64编码的签名结果
     */
    public static String doSign(String content, String privateKeyStr, String encode) {
        if (StringUtils.isAnyBlank(content, privateKeyStr, encode)) {
            log.warn("签名参数为空: content={}, privateKeyStr={}, encode={}", content, privateKeyStr, encode);
            return null;
        }

        try {
            byte[] privateKeys = Base64.getDecoder().decode(privateKeyStr.getBytes(StandardCharsets.UTF_8));
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeys);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(content.getBytes(encode));
            byte[] signed = signature.sign();
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            log.error("RSA签名失败: content={}, error={}", content, e.getMessage(), e);
            return null;
        }
    }

    /**
     * RSA签名验证
     * 使用公钥验证签名
     *
     * @param content 原始内容
     * @param signed Base64编码的签名
     * @param publicKeyStr Base64编码的公钥
     * @param encode 字符编码
     * @return 验证结果
     */
    public static boolean doCheck(String content, String signed, String publicKeyStr, String encode) {
        if (StringUtils.isAnyBlank(content, signed, publicKeyStr, encode)) {
            log.warn("验签参数为空: content={}, signed={}, publicKeyStr={}, encode={}", 
                    content, signed, publicKeyStr, encode);
            return false;
        }

        try {
            byte[] publicKeys = Base64.getDecoder().decode(publicKeyStr.getBytes(StandardCharsets.UTF_8));
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeys);
            KeyFactory myKeyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicKey = myKeyFactory.generatePublic(publicKeySpec);

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(content.getBytes(encode));
            return signature.verify(Base64.getDecoder().decode(signed));
        } catch (Exception e) {
            log.error("RSA验签失败: content={}, error={}", content, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成待签名字符串
     * 按照指定格式拼接参数：POST:endpointUrl:accessToken:hexString:timeStamp
     *
     * @param endpointUrl 接口地址
     * @param accessToken 访问令牌
     * @param jsonStr JSON字符串
     * @param timeStamp 时间戳
     * @return 待签名字符串
     */
    @SneakyThrows
    public static String stringToSign(String endpointUrl, String accessToken, String jsonStr, String timeStamp) {
        if (StringUtils.isAnyBlank(endpointUrl, accessToken, jsonStr, timeStamp)) {
            log.warn("生成待签名字符串参数为空: endpointUrl={}, accessToken={}, jsonStr={}, timeStamp={}", 
                    endpointUrl, accessToken, jsonStr, timeStamp);
            return null;
        }

        String minify = minify(jsonStr);
        String hexString = byte2Hex(SHA256(minify)).toLowerCase();
        log.debug("生成待签名字符串: minify={}, hexString={}", minify, hexString);
        return "POST" + ":" + endpointUrl + ":" + accessToken + ":" + hexString + ":" + timeStamp;
    }

    /**
     * SHA256哈希
     * 计算字符串的SHA256哈希值
     *
     * @param requestBody 待计算哈希的字符串
     * @return 哈希值字节数组
     * @throws NoSuchAlgorithmException 如果算法不存在
     */
    public static byte[] SHA256(String requestBody) throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(requestBody)) {
            log.warn("SHA256哈希参数为空");
            return new byte[0];
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(requestBody.getBytes());
    }

    /**
     * 字节数组转16进制字符串
     * 将字节数组转换为小写的16进制字符串
     *
     * @param bytes 字节数组
     * @return 16进制字符串
     */
    private static String byte2Hex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                stringBuilder.append("0");
            }
            stringBuilder.append(temp);
        }
        return stringBuilder.toString();
    }

    /**
     * HmacSHA512签名
     * 使用密钥对数据进行HmacSHA512签名
     *
     * @param signData 待签名数据
     * @param secret 密钥
     * @return Base64编码的签名结果
     */
    @SneakyThrows
    public static String hmacSHA512(String signData, String secret) {
        if (StringUtils.isAnyBlank(signData, secret)) {
            log.warn("HmacSHA512签名参数为空: signData={}, secret={}", signData, secret);
            return null;
        }

        Mac hMacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
        hMacSha512.init(keySpec);
        byte[] signatureBytes = hMacSha512.doFinal(signData.getBytes());
        return java.util.Base64.getEncoder().encodeToString(signatureBytes);
    }

    /**
     * JSON字符串压缩
     * 移除JSON字符串中的空白字符和注释
     *
     * @param jsonString JSON字符串
     * @return 压缩后的JSON字符串
     */
    public static String minify(String jsonString) {
        if (StringUtils.isBlank(jsonString)) {
            return "";
        }

        boolean in_string = false;
        boolean in_multi_line_comment = false;
        boolean in_single_line_comment = false;
        char string_opener = 'x'; // unused value, just something that makes compiler happy

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < jsonString.length(); i++) {
            // get next (c) and next-next character (cc)
            char c = jsonString.charAt(i);
            char cc = (i + 1 < jsonString.length()) ? jsonString.charAt(i + 1) : ' ';

            // handle string
            if (c == '"' || c == '\'') {
                if (!in_multi_line_comment && !in_single_line_comment) {
                    if (!in_string) {
                        in_string = true;
                        string_opener = c;
                    } else if (string_opener == c) {
                        in_string = false;
                    }
                }
            }

            // handle multi-line comment
            else if (c == '/' && cc == '*' && !in_string) {
                in_multi_line_comment = true;
                i++;
                continue;
            } else if (c == '*' && cc == '/' && in_multi_line_comment) {
                in_multi_line_comment = false;
                i++;
                continue;
            }

            // handle single-line comment
            else if (c == '/' && cc == '/' && !in_string) {
                in_single_line_comment = true;
                i++;
                continue;
            } else if (c == '\n' && in_single_line_comment) {
                in_single_line_comment = false;
                continue;
            }

            // handle whitespace
            else if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                if (!in_string && !in_multi_line_comment && !in_single_line_comment) {
                    continue;
                }
            }

            // handle everything else
            if (!in_multi_line_comment && !in_single_line_comment) {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static void main(String[] args) {
        String path = "/v1.0/transaction/pay-in";
        String accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                ".eyJuYmYiOjE2ODk5MTg0OTcsImV4cCI6MTY4OTkxOTM5NywiaWF0IjoxNjg5OTE4NDk3LCJNRVJDSEFOVF9JRCI6IjEwMDAxIn0" +
                ".Wwa2-P_Rz79JG63yQZESM40pBAGf0dpVvQgmeRoOl8E";
        String jsonStr = "{\n" +
                "    \"originalPartnerReferenceNo\":\"123\"\n" +
                "}";
        String timeStamp = "2020-12-17T10:55:00+07:00";
        String merchantSecret = "60e070fc44e4188c0b08fc5c0a9b975f1c0a11facbff89e5e2c24c729d0cdce9";
        merchantSecret = "9b04930df7801b79bf75e1dbbed9c42a4d1838d96a22f24846c9c40c38448b6c";
        String stringToSign = stringToSign(path, accessToken, jsonStr, timeStamp);
        String signature = SignUtil.hmacSHA512(stringToSign, merchantSecret);
        System.out.println("signature = " + signature);
    }
}