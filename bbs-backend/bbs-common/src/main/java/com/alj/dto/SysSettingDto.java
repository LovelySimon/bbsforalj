package com.alj.dto;

/**
 * @description: 总体设置
 * @author alj
 * @date 2024/4/2 14:51
 * @version 1.0
 */
public class SysSettingDto {
    private SysSettingAudit sysSettingAudit;
    private SysSettingComment sysSettingComment;
    private SysSettingLike sysSettingLike;
    private SysSettingPost sysSettingPost;
    private SysSettingRegister sysSettingRegister;
    private SysSettingsEmail sysSettingsEmail;

    public SysSettingAudit getSysSettingAudit() {
        return sysSettingAudit;
    }

    public void setSysSettingAudit(SysSettingAudit sysSettingAudit) {
        this.sysSettingAudit = sysSettingAudit;
    }

    public SysSettingComment getSysSettingComment() {
        return sysSettingComment;
    }

    public void setSysSettingComment(SysSettingComment sysSettingComment) {
        this.sysSettingComment = sysSettingComment;
    }

    public SysSettingLike getSysSettingLike() {
        return sysSettingLike;
    }

    public void setSysSettingLike(SysSettingLike sysSettingLike) {
        this.sysSettingLike = sysSettingLike;
    }

    public SysSettingPost getSysSettingPost() {
        return sysSettingPost;
    }

    public void setSysSettingPost(SysSettingPost sysSettingPost) {
        this.sysSettingPost = sysSettingPost;
    }

    public SysSettingRegister getSysSettingRegister() {
        return sysSettingRegister;
    }

    public void setSysSettingRegister(SysSettingRegister sysSettingRegister) {
        this.sysSettingRegister = sysSettingRegister;
    }

    public SysSettingsEmail getSysSettingsEmail() {
        return sysSettingsEmail;
    }

    public void setSysSettingsEmail(SysSettingsEmail sysSettingsEmail) {
        this.sysSettingsEmail = sysSettingsEmail;
    }
}
