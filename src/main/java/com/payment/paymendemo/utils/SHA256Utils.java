package com.payment.paymendemo.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.ObjectUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SHA256Utils {

    private static Logger logger = LogManager.getLogger(SHA256Utils.class);
    private static final String ALGORITHM = "HmacSHA256";


    /**
     * 生成签名（SHA256）
     *
     * @param paramMap 待签名数据
     * @param key  API密钥
     * @return 签名
     */
    public static String generateSignature(final Map<String, Object> paramMap, String key) {
        SortedMap<String, Object> data = new TreeMap<String, Object>(paramMap);
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyArray.length; i++) {
            String k = keyArray[i];
            if ("sign".equals(k)) {
                continue;
            }
            // 参数值为空，则不参与签名
            if (!ObjectUtils.isEmpty(data.get(k))) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(k).append("=").append(data.get(k).toString().trim());
            }

        }
        System.out.println(String.format("\n签名字符串：%s \n", sb.toString().trim()));
        return sign(sb.toString(), key);
    }

    public static boolean valid(String message, String secret, String signature) {
        return signature != null && signature.equals(sign(message, secret));
    }

    public static String sign(String message, String secret) {
        try {

            Mac hmac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            hmac.init(secret_key);
            byte[] bytes = hmac.doFinal(message.getBytes());
            logger.info("service sign is " + byteArrayToHexString(bytes));
            return byteArrayToHexString(bytes);
        } catch (Exception ex) {
            logger.error("签名错误：", ex);
        }
        return null;
    }

    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder hs = new StringBuilder();
        String tempStr;
        for (int index = 0; bytes != null && index < bytes.length; index++) {
            tempStr = Integer.toHexString(bytes[index] & 0XFF);
            if (tempStr.length() == 1)
                hs.append('0');
            hs.append(tempStr);
        }
        return hs.toString().toLowerCase();
    }

    public static void main(String[] args) {
        Map<String, Object> data = new HashMap<>();
        data.put("amount", "500.05");
        data.put("currency", "WP");
        data.put("orderno", "FOXaTxWRnG");
        String sign = SHA256Utils.generateSignature(data, "DTbflFhO");
        System.out.println(sign);

        Map<String, Object> data2 = new HashMap<>();
        data2.put("amount", "1000.00");
        data2.put("currency", "WP");
        data2.put("orderno", "pxW7hrN7Cg");
//        data2.put("status", "1");
//        data2.put("sign", "a6f1c50f8bea0c805dff5988c23ae7cb34bd93fc1e20344619338c5d5ad14ba9");
        sign = SHA256Utils.generateSignature(data2, "DTbflFhO");
        System.out.println(sign);
    }

}
