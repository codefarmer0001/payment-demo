package com.payment.paymendemo.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.payment.paymendemo.utils.HttpUtil;
import com.payment.paymendemo.utils.Md5Util;
import com.payment.paymendemo.utils.SHA256Utils;
import com.payment.paymendemo.utils.SignUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.httpclient.NameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
public class OrderController {


    //商户号，需要平台方提供，与 secret 对应
//    private final String merchantNo = "YJ749567";
    //币种id
    private final Integer currencyId = 2;
    //支付成功后通知地址
    private final String notifyUrl = "https://www.baidu.com";
    //支付成功后跳转地址
    private final String returnUrl = "https://www.google.com.hk";
    //签名的key，需要平台方提供，与 merchantNo 对应
//    private final String secret = "177965d903f446bd98b8facbfa361b96";
    //统一的下单接口
    @Value("${pay.server.out}")
    private String orderUrl;
    //统一的代付接口
    @Value("${pay.server.out}")
    private String payOutUrl;

    private static final Map<String, String> encryptTypeMap = new HashMap<>();

    static {
        encryptTypeMap.put("MD5", "1");
        encryptTypeMap.put("sha256", "2");
    }

    /**
     * 获取客户端真实IP地址
     */
    protected String getClientIpAddress(HttpServletRequest request) {
        // 1. 尝试从X-Forwarded-For获取（经过代理时使用）
        String ip = request.getHeader("X-Forwarded-For");

        // 2. 如果X-Forwarded-For为空，尝试从Proxy-Client-IP获取
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        // 3. 如果还是空，尝试从WL-Proxy-Client-IP获取
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        // 4. 如果还是空，尝试从HTTP_CLIENT_IP获取
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }

        // 5. 如果还是空，尝试从HTTP_X_FORWARDED_FOR获取
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        // 6. 最后使用request.getRemoteAddr()
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多级代理，取第一个非unknown的IP
        if (ip != null && ip.contains(",")) {
            ip = Arrays.stream(ip.split(","))
                    .map(String::trim)
                    .filter(i -> !"unknown".equalsIgnoreCase(i))
                    .findFirst()
                    .orElse(request.getRemoteAddr());
        }

        // 处理本地测试的IPv6地址
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }

    @PostMapping("/create")
    public Object createOrder(@RequestBody Map<String, String> map, HttpServletRequest request) {

        Map<String, Object> resultMap = new HashMap<>();
        String encryptType = map.get("encryptType");
        BigDecimal amount = new BigDecimal(map.get("amount"));
        if(ObjectUtils.isEmpty(amount)) {
            resultMap.put("code", 500);
            resultMap.put("msg", "支付金额不能为空");
            return resultMap;
        }

        if(amount.compareTo(BigDecimal.ZERO) < 0) {
            resultMap.put("code", 500);
            resultMap.put("msg", "支付金额必须大于0");
            return resultMap;
        }

        String merchantNo =  map.get("merchantNo");
        if(ObjectUtils.isEmpty(merchantNo)) {
            resultMap.put("code", 500);
            resultMap.put("msg", "商户号不能为空");
            return resultMap;
        }

        String secret = map.get("secret");
        if(ObjectUtils.isEmpty(secret)) {
            resultMap.put("code", 500);
            resultMap.put("msg", "加密secret不能为空");
            return resultMap;
        }

        Map paramMap = new HashMap();
        paramMap.put("amount", amount);
        paramMap.put("currencyId", currencyId);
        paramMap.put("clientIp", getClientIpAddress(request));
        paramMap.put("notifyUrl", notifyUrl);
        paramMap.put("returnUrl", returnUrl);
        paramMap.put("merchantNo", merchantNo);
        paramMap.put("merchantOrderNo", UUID.randomUUID().toString().replaceAll("-", ""));

        String signStr = "";
        if("md5".equals(encryptType)) {
            paramMap.put("encryptType", encryptTypeMap.get("MD5"));
            signStr = SignUtil.sign(paramMap, secret, "key");
        }

        if("sha256".equals(encryptType)) {
            paramMap.put("encryptType", encryptTypeMap.get("SHA256"));
            signStr = SHA256Utils.generateSignature(paramMap, secret);
        }

        paramMap.put("sign", signStr);

        String response = HttpUtil.sendPost(orderUrl, paramMap);
        JSONObject json = JSON.parseObject(response);

        return json;
    }

    @PostMapping("/payOut")
    public Object payOut(@RequestBody Map<String, String> map, HttpServletRequest request) {

        Map<String, Object> resultMap = new HashMap<>();
        String encryptType = map.get("encryptType");
        BigDecimal amount = new BigDecimal(map.get("amount"));
        if(ObjectUtils.isEmpty(amount)) {
            resultMap.put("code", 500);
            resultMap.put("msg", "支付金额不能为空");
            return resultMap;
        }

        if(amount.compareTo(BigDecimal.ZERO) < 0) {
            resultMap.put("code", 500);
            resultMap.put("msg", "支付金额必须大于0");
            return resultMap;
        }

        String merchantNo =  map.get("merchantNo");
        if(ObjectUtils.isEmpty(merchantNo)) {
            resultMap.put("code", 500);
            resultMap.put("msg", "商户号不能为空");
            return resultMap;
        }

        String secret = map.get("secret");
        if(ObjectUtils.isEmpty(secret)) {
            resultMap.put("code", 500);
            resultMap.put("msg", "加密secret不能为空");
            return resultMap;
        }

        Map paramMap = new HashMap();
        paramMap.put("amount", amount);
        paramMap.put("currencyId", currencyId);
        paramMap.put("clientIp", getClientIpAddress(request));
        paramMap.put("notifyUrl", notifyUrl);
        paramMap.put("merchantNo", merchantNo);
        paramMap.put("merchantOrderNo", UUID.randomUUID().toString().replaceAll("-", ""));

        paramMap.put("phone", map.get("phone"));
        paramMap.put("email", map.get("email"));
        paramMap.put("account", map.get("account"));
        paramMap.put("accountName", map.get("accountName"));
        paramMap.put("address", map.get("address"));
        paramMap.put("subBranch", map.get("subBranch"));
        paramMap.put("withdrawType", map.get("withdrawType"));
        paramMap.put("bankName", map.get("bankName"));

        String signStr = "";
        if("md5".equals(encryptType)) {
            paramMap.put("encryptType", encryptTypeMap.get("MD5"));
            signStr = SignUtil.sign(paramMap, secret, "key");
        }

        if("sha256".equals(encryptType)) {
            paramMap.put("encryptType", encryptTypeMap.get("SHA256"));
            signStr = SHA256Utils.generateSignature(paramMap, secret);
        }

        paramMap.put("sign", signStr);

        String response = HttpUtil.sendPost(payOutUrl, paramMap);
        JSONObject json = JSON.parseObject(response);

        return json;
    }


}
