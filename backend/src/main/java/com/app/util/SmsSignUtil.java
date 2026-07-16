package com.app.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 联麓短信 API 签名工具
 * 文档附录：签名机制详解
 */
public class SmsSignUtil {

    private static final String[] EXCLUDE_KEYS = {
        "Signature", "SessionContext", "PhoneNumberSet", "SessionContextSet",
        "ContextParamSet", "TemplateParamSet", "PhoneList", "phoneSet"
    };

    /**
     * 生成 MD5 签名
     * @param params 请求参数 (已包含所有业务参数)
     * @param appKey 平台密钥
     * @return 大写 MD5 签名
     */
    public static String sign(Map<String, Object> params, String appKey) {
        try {
            TreeMap<String, Object> sorted = new TreeMap<>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                // 过滤排除的 key
                if (isExcluded(key)) continue;
                // 过滤 null 或空字符串
                if (value == null || "".equals(value.toString())) continue;
                sorted.put(key, value);
            }
            // 按 key 排序拼接 &key=AppKey
            String str = sorted.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&")) + "&key=" + appKey;

            return md5(str).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 signing failed", e);
        }
    }

    private static boolean isExcluded(String key) {
        for (String ek : EXCLUDE_KEYS) {
            if (ek.equals(key)) return true;
        }
        return false;
    }

    private static String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
