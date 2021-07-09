package com.reanon.community.utils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 封装一些通用的方法
 *
 * @author reanon
 * @create 2021-07-02
 */
public class CommunityUtil {
    /**
     * 生成随机字符串
     */
    public static String generateUUID() {
        // 去除生成的随机字符串中的 ”-“
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * md5 对密码进行加密
     *
     * @param key 要加密的字符串
     */
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 将服务端返回的消息封装成 JSON 格式的字符串
     *
     * @param code 状态码
     * @param msg  提示消息
     * @param map  业务数据
     * @return 返回 JSON 格式字符串
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    // 重载 getJSONString 方法, s服务端方法可能不返回业务数据
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    // 重载 getJSONString 方法, 服务端方法可能不返回业务数据和提示消息
    public static String getJSONString(int code) throws JSONException {
        return getJSONString(code, null, null);
    }

    // editor.md 要求返回的 JSON 字符串格式
    public static String getEditorMdJSONString(int success, String message, String url) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("success", success);
        json.put("message", message);
        json.put("url", url);
        return json.toJSONString();
    }

    /**
     * 测试
     *
     * @param args
     */
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Jack");
        map.put("age", 18);
        // 结果: {"msg":"ok","code":0,"name":"Jack","age":18}
        System.out.println(getJSONString(0, "ok", map));
    }
}
