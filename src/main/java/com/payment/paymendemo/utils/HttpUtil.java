package com.payment.paymendemo.utils;


import com.alibaba.fastjson2.JSON;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpUtil {

    public static String methodPost(String url, NameValuePair[] data) {

        String response = "";//要返回的response信息
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(url);
        // 将表单的值放入postMethod中
        postMethod.setRequestBody(data);
        postMethod.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        // 执行postMethod
        int statusCode = 0;
        try {
            statusCode = httpClient.executeMethod(postMethod);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // HttpClient对于要求接受后继服务的请求，象POST和PUT等不能自动处理转发
        // 301或者302
        if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
                || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
            // 从头中取出转向的地址
            Header locationHeader = postMethod.getResponseHeader("location");
            String location = null;
            if (locationHeader != null) {
                location = locationHeader.getValue();
                System.out.println("The page was redirected to:" + location);
                response = methodPost(location, data);//用跳转后的页面重新请求。
            } else {
                System.err.println("Location field value is null.");
            }
        } else {
            System.out.println(postMethod.getStatusLine());

            try {
                response = postMethod.getResponseBodyAsString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            postMethod.releaseConnection();
        }
        return response;
    }


    public static NameValuePair[] mapToNameValuePairArray(Map<String, Object> data) {
        Set<Map.Entry<String, Object>> entrySet = data.entrySet();
        int size = entrySet.size();
        NameValuePair[] nameValuePairs = new NameValuePair[size];
        List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> entry : entrySet) {
            String key = entry.getKey();
            String value = String.valueOf(entry.getValue());
            NameValuePair nameValuePair = new NameValuePair(key, value);
            nameValuePairList.add(nameValuePair);
        }
        for (int i = 0; i < nameValuePairList.size(); i++) {
            nameValuePairs[i] = nameValuePairList.get(i);
        }
        return nameValuePairs;
    }


    /**
     * @return java.lang.String
     * @Author Allen
     * @Description 发送HttpPost请求，参数为map
     * @Date 2020/4/15 14:27
     * @Param [url, map]
     **/
    public static String sendPost(String url, Map<String, Object> map) {
        CloseableHttpResponse response = null;
        String resData = "";
        try {
            //编码格式
            String charset = "UTF-8";

            //请求内容
            String content = JSON.toJSONString(map);

            //使用帮助类HttpClients创建CloseableHttpClient对象.
            CloseableHttpClient client = HttpClients.createDefault();

            //HTTP请求类型创建HttpPost实例
            HttpPost post = new HttpPost(url);

            //使用addHeader方法添加请求头部,诸如User-Agent, Accept-Encoding等参数.
            post.setHeader("Content-Type", "application/json;charset=UTF-8");

            //组织数据
            StringEntity se = new StringEntity(content);

            //设置编码格式
            se.setContentEncoding(charset);

            //设置数据类型
            se.setContentType("application/json");

            //对于POST请求,把请求体填充进HttpPost实体.
            post.setEntity(se);

            //通过执行HttpPost请求获取CloseableHttpResponse实例 ,从此CloseableHttpResponse实例中获取状态码,错误信息,以及响应页面等等.
            response = client.execute(post);

            //通过HttpResponse接口的getEntity方法返回响应信息，并进行相应的处理 
            resData = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
        }

        return resData;
    }


}
