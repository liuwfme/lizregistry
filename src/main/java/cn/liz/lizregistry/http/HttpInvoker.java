package cn.liz.lizregistry.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker Default = new OkHttpInvoker(500);

    String post(String requestStr, String url);

    String get(String url);

    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug("====== HttpInvoker#httpGet url:{},clazz:{}", url, clazz);
        String respJson = Default.get(url);
        log.debug("====== HttpInvoker#httpGet response:{}", url);
        return JSON.parseObject(respJson, clazz);
    }

    static <T> T httpGet(String url, TypeReference<T> clazz) {
        log.debug("====== HttpInvoker#httpGet url:{},TypeReference:{}", url, clazz);
        String respJson = Default.get(url);
        log.debug("====== HttpInvoker#httpGet response:{}", url);
        return JSON.parseObject(respJson, clazz);
    }

    static <T> T httpPost(String url, String requestStr, Class<T> clazz) {
        log.debug("====== HttpInvoker#httpPost url:{},requestStr:{},clazz:{}", url, requestStr, clazz);
        String respJson = Default.post(requestStr, url);
        log.debug("====== HttpInvoker#httpPost response:{}", respJson);
        return JSON.parseObject(respJson, clazz);
    }
}
