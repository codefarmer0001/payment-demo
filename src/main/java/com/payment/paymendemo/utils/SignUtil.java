package com.payment.paymendemo.utils;

import io.micrometer.common.util.StringUtils;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class SignUtil {


    public static String getValueNullSignKey(Map<String, Object> paramMap, String paySecrete) {
        SortedMap<String, Object> smap = new TreeMap<String, Object>(paramMap);    // 每一个值从 a 到 z 的顺序排序，若遇到相同首字母，则看第二个字母，以此类推。
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, Object> m : smap.entrySet()) {
            if ("sign".equals(m.getKey())) {    // 排除sign参数
                continue;
            }
            stringBuffer.append(m.getKey()).append("=").append(m.getValue()).append("&");    // 所有数组值以“&”字符连接起来
        }
        stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());

        String argPreSign = stringBuffer.append(paySecrete).toString();    // 最后加上支付密匙paySecret
        String signStr = Md5Util.md5Hex(argPreSign).toLowerCase();    // MD5小写
        return signStr;
    }

    /**
     * @Description 空参数也参与签名
     **/
    public static String getValueNullSign(Map<String, Object> paramMap, String paySecret, String keyName) {
        SortedMap<String, Object> smap = new TreeMap<String, Object>(paramMap);    // 每一个值从 a 到 z 的顺序排序，若遇到相同首字母，则看第二个字母，以此类推。
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, Object> m : smap.entrySet()) {
            if ("sign".equals(m.getKey())) {    // 排除sign参数
                continue;
            }
            stringBuffer.append(m.getKey()).append("=").append(m.getValue()).append("&");    // 所有数组值以“&”字符连接起来
        }
        stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());

        String argPreSign = stringBuffer.append("&").append(keyName).append("=").append(paySecret).toString();    // 最后加上支付密匙paySecret
        String signStr = Md5Util.md5Hex(argPreSign).toLowerCase();    // MD5小写
        return signStr;
    }


    /**
     * @Description 空参数也参与签名
     **/
    public static String sign(Map<String, Object> paramMap, String paySecret, String keyName) {
        SortedMap<String, Object> smap = new TreeMap<String, Object>(paramMap);    // 每一个值从 a 到 z 的顺序排序，若遇到相同首字母，则看第二个字母，以此类推。
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, Object> m : smap.entrySet()) {
            if ("sign".equals(m.getKey())) {    // 排除sign参数
                continue;
            }
            Object value = m.getValue();
            if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
                stringBuffer.append(m.getKey()).append("=").append(m.getValue()).append("&");     // 所有数组值以“&”字符连接起来
            }
        }
//        stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());

        String argPreSign = stringBuffer.append("&").append(keyName).append("=").append(paySecret).toString();
//        String argPreSign = stringBuffer.append(paySecret).toString();    // 最后加上支付密匙paySecret
        String signStr = Md5Util.md5Hex(argPreSign).toLowerCase();    // MD5小写
        return signStr;
    }


}
