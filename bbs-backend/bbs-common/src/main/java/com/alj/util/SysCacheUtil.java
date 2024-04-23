package com.alj.util;

import com.alj.dto.SysSettingDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 刷新缓存
 * @author alj
 * @date 2024/4/3 13:34
 * @version 1.0
 */
public class SysCacheUtil {
    private static final Map<String, SysSettingDto> cache=new ConcurrentHashMap<>();
    private  static final String key="sys_setting";

    public static SysSettingDto getSysSetting(){
        return cache.get(key);
    }
    public static void refresh(SysSettingDto dto){
        cache.put(key,dto);
    }
}
