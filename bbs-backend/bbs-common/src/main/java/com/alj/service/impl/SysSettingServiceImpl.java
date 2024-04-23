package com.alj.service.impl;

import com.alj.dto.*;
import com.alj.enums.SysSettingCode;
import com.alj.exception.BusinessException;
import com.alj.pojo.SysSetting;
import com.alj.mapper.SysSettingMapper;
import com.alj.service.SysSettingService;
import com.alj.util.JsonUtil;
import com.alj.util.StringTools;
import com.alj.util.SysCacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * <p>
 * 系统设置信息 服务实现类
 * </p>
 *
 * @author LovelySimon
 * @since 2024-02-08
 */
@Service
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService {
    private static final Logger logger= LoggerFactory.getLogger(SysSettingServiceImpl.class);
    @Resource
    private SysSettingMapper sysSettingMapper;

    /**
     * @description: 更新系统设置缓存
     * @author alj
     * @date 2024/4/3 13:58
     * @version 1.0
     */
    @Override
    public SysSettingDto refreshCache() {
        try{
            SysSettingDto sysSettingDto=new SysSettingDto();
            List<SysSetting> list=this.sysSettingMapper.selectList(null);
            for (SysSetting sysSetting:list){
                String jsonContent=sysSetting.getJsonContent();
                if (StringTools.isEmpty(jsonContent)){
                    continue;
                }
                String code=sysSetting.getCode();
                SysSettingCode sysSettingCode=SysSettingCode.getBycode(code);
                PropertyDescriptor pd=new PropertyDescriptor(sysSettingCode.getPropName(), SysSettingDto.class);
                Method method=pd.getWriteMethod();
                Class classz=Class.forName(sysSettingCode.getClassz());
                method.invoke(sysSettingDto,JsonUtil.convertJSON2obj(jsonContent,classz));
            }
            SysCacheUtil.refresh(sysSettingDto);
            return sysSettingDto;
        }catch (Exception e){
            logger.error("刷新缓存失败",e);
            throw new BusinessException("刷新缓存失败");
        }

    }

    /**
     * @description: 用反射保存设置
     * @author alj
     * @date 2024/4/16 14:01
     * @version 1.0
     */
    @Override
    public void saveSetting(SysSettingDto sysSettingDto) {
        try {
            Class classz=SysSettingDto.class;
            for (SysSettingCode codeenum:SysSettingCode.values()){
                PropertyDescriptor pd=new PropertyDescriptor(codeenum.getPropName(),classz);
                Method method=pd.getReadMethod();
                Object object=method.invoke(sysSettingDto);
                SysSetting sysSetting=new SysSetting();
                sysSetting.setCode(codeenum.getCode());
                sysSetting.setJsonContent(JsonUtil.convertObj2Json(object));
                this.sysSettingMapper.update(sysSetting,new UpdateWrapper<SysSetting>().eq("code",sysSetting.getCode()));
            }
        }catch (Exception e){
            logger.error("保存设置失败",e);
            throw new BusinessException("保存设置失败");
        }
    }
}
