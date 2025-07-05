package com.payment.paymendemo.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.payment.paymendemo.utils.HttpUtil;
import com.payment.paymendemo.utils.Md5Util;
import com.payment.paymendemo.utils.SHA256Utils;
import com.payment.paymendemo.utils.SignUtil;
import org.apache.commons.httpclient.NameValuePair;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
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
    private static final String orderUrl = "http://192.168.0.14:8082/v1/order/create";

    private static final Map<String, String> encryptTypeMap = new HashMap<>();

    static {
        encryptTypeMap.put("MD5", "1");
        encryptTypeMap.put("sha256", "2");
    }


    @PostMapping("/create")
    public Object createOrder(@RequestBody Map<String, String> map) {

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
        paramMap.put("clientIp", "127.0.0.1");
        paramMap.put("notifyUrl", notifyUrl);
        paramMap.put("returnUrl", returnUrl);
        paramMap.put("merchantNo", merchantNo);
        paramMap.put("merchantOrderNo", UUID.randomUUID().toString().replaceAll("-", ""));

        String signStr = "";
        if("md5".equals(encryptType)) {
            paramMap.put("encryptType", encryptTypeMap.get("md5"));
            signStr = SignUtil.sign(paramMap, secret, "key");
        }

        if("sha256".equals(encryptType)) {
            paramMap.put("encryptType", encryptTypeMap.get("sha256"));
            signStr = SHA256Utils.generateSignature(paramMap, secret);
        }

        paramMap.put("sign", signStr);

        String request = HttpUtil.sendPost(orderUrl, paramMap);
        JSONObject json = JSON.parseObject(request);

        return json;
    }

}
