package com.alj.enums;

import com.alj.dto.SysSettingComment;

public enum SysSettingCode {
    AUDIT("audit","com.alj.dto.SysSettingAudit","sysSettingAudit","审核设置"),
    COMMENT("comment","com.alj.dto.SysSettingComment","sysSettingComment","评论设置"),
    POST("post","com.alj.dto.SysSettingPost","sysSettingPost","帖子设置"),
    LIKE("like","com.alj.dto.SysSettingLike","sysSettingLike","点赞设置"),
    REGISTER("register","com.alj.dto.SysSettingRegister","sysSettingRegister","注册设置"),
    EMAIL("email","com.alj.dto.SysSettingsEmail","sysSettingsEmail","邮件设置");

    private String code;
    private String classz;
    private String propName;
    private String desc;

    public static SysSettingCode getBycode(String code){
        for (SysSettingCode item:SysSettingCode.values()){
            if (item.getCode().equals(code)){
                return item;
            }
        }
        return null;
    }

    SysSettingCode(String code, String classz, String propName, String desc) {
        this.code = code;
        this.classz = classz;
        this.propName = propName;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getClassz() {
        return classz;
    }

    public String getPropName() {
        return propName;
    }

    public String getDesc() {
        return desc;
    }
}
