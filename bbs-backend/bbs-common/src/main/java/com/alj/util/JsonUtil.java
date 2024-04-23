package com.alj.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonUtil {
    public static final Logger logger=LoggerFactory.getLogger(JsonUtil.class);

    /**
     * @description:字符串转为json
     * @author alj
     * @date 2024/4/2 15:45
     * @version 1.0
     */
    public static String convertObj2Json(Object ob){
        return JSON.toJSONString(ob);
    }

    /**
     * @description: json转字符串
     * @author alj
     * @date 2024/4/2 15:45
     * @version 1.0
     */
    public static <T> T convertJSON2obj(String json,Class<T> classz){
        return JSONObject.parseObject(json,classz);
    }

    /**
     * @description: 字符串数组转对象
     * @author alj
     * @date 2024/4/2 15:48
     * @version 1.0
     */
    public static <T> List<T> convertJSONArray2List(String json,Class<T> classz){
        return JSONArray.parseArray(json,classz);
    }
}
