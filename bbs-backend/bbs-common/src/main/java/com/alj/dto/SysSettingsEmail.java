package com.alj.dto;


/**
 * @description: 邮箱控制
 * @author alj
 * @date 2024/4/2 14:32
 * @version 1.0
 */
public class SysSettingsEmail {
    //标题
    private String emailTitle;
    //内容
    private String emailContent;

    public String getEmailTitle() {
        return emailTitle;
    }

    public void setEmailTitle(String emailTitle) {
        this.emailTitle = emailTitle;
    }

    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }
}
