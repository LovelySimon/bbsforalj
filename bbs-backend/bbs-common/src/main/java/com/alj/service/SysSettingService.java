package com.alj.service;

import com.alj.dto.SysSettingDto;
import com.alj.pojo.SysSetting;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 系统设置信息 服务类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
public interface SysSettingService extends IService<SysSetting> {
    SysSettingDto refreshCache();

    void saveSetting(SysSettingDto sysSettingDto);

}
