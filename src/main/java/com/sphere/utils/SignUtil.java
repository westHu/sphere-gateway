package com.sphere.utils;


import com.sphere.exception.GatewayException;
import com.sphere.exception.GatewayExceptionCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 功能：SHA256withRSA 工具类
 *
 * @author west
 */

@Slf4j
public class SignUtil {

    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private SignUtil() {
        throw new GatewayException("Utility classes should not have public constructors");
    }

    /**
     * 加签：生成报文签名
     *
     * @param content       报文内容
     * @param privateKeyStr 私钥
     * @param encode        编码
     */
    public static String doSign(String content, String privateKeyStr, String encode) {
        try {
            //String unsigned = Base64.encodeBase64String(content.getBytes(encode));
            byte[] privateKeys = Base64.decodeBase64(privateKeyStr.getBytes());
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeys);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(content.getBytes(encode));
            byte[] signed = signature.sign();
            return Base64.encodeBase64String(signed);
        } catch (Exception e) {
            log.error("Sign exception", e);
        }
        return null;
    }

    /**
     * 验证：验证签名信息
     *
     * @param content      签名报文
     * @param signed       签名信息
     * @param publicKeyStr 公钥
     * @param encode       编码格式
     */
    public static boolean doCheck(String content, String signed, String publicKeyStr, String encode) {
        try {
            byte[] publicKeys = Base64.decodeBase64(publicKeyStr);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeys);
            KeyFactory myKeyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicKey = myKeyFactory.generatePublic(publicKeySpec);

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(content.getBytes(encode));
            return signature.verify(Base64.decodeBase64(signed));
        } catch (Exception e) {
            log.error("Sign doCheck exception:", e);
        }
        return false;
    }


    /**
     * stringToSign
     */
    @SneakyThrows
    public static String stringToSign(String endpointUrl, String accessToken, String jsonStr, String timeStamp) {
        String minify = minify(jsonStr);
        String hexString = byte2Hex(SHA256(minify)).toLowerCase();
        log.info("stringToSign minify={}, hexString={}", minify, hexString);
        return "POST" + ":" + endpointUrl + ":" + accessToken + ":" + hexString + ":" + timeStamp;
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


    /**
     * SHA-256
     */
    public static byte[] SHA256(String requestBody) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(requestBody.getBytes());
    }


    /**
     * sha256加密 将byte转为16进制
     */
    private static String byte2Hex(byte[] bytes) {
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
     * HmacSHA512算法验证签名
     */
    @SneakyThrows
    public static String hmacSHA512(String signData, String secret) {
        Mac hMacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
        hMacSha512.init(keySpec);
        byte[] signatureBytes = hMacSha512.doFinal(signData.getBytes());
        return java.util.Base64.getEncoder().encodeToString(signatureBytes);
    }


    /**
     * minify
     */
    public static String minify(String jsonString) {
        boolean in_string = false;
        boolean in_multi_line_comment = false;
        boolean in_single_line_comment = false;
        char string_opener = 'x'; // unused value, just something that makes compiler happy

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < jsonString.length(); i++) {
            // get next (c) and next-next character (cc)

            char c = jsonString.charAt(i);
            String cc = jsonString.substring(i, Math.min(i + 2, jsonString.length()));

            // big switch is by what mode we're in (in_string etc.)
            if (in_string) {
                if (c == string_opener) {
                    in_string = false;
                    out.append(c);
                } else if (c == '\\') { // no special treatment needed for \\u, it just works like this too
                    out.append(cc);
                    ++i;
                } else
                    out.append(c);
            } else if (in_single_line_comment) {
                if (c == '\r' || c == '\n')
                    in_single_line_comment = false;
            } else if (in_multi_line_comment) {
                if (cc.equals("*/")) {
                    in_multi_line_comment = false;
                    ++i;
                }
            } else {
                // we're outside of the special modes, so look for mode openers (comment start, string start)
                if (cc.equals("/*")) {
                    in_multi_line_comment = true;
                    ++i;
                } else if (cc.equals("//")) {
                    in_single_line_comment = true;
                    ++i;
                } else if (c == '"' || c == '\'') {
                    in_string = true;
                    string_opener = c;
                    out.append(c);
                } else if (!Character.isWhitespace(c))
                    out.append(c);
            }
        }
        return out.toString();
    }

}