package com.alj.dto;

/**
 * @description: 发帖设置
 * @author alj
 * @date 2024/4/2 14:36
 * @version 1.0
 */
public class SysSettingPost {
    //发帖积分
    private Integer postIntegral;
    //发帖数量阈值
    private Integer postDayCountThreshold;
    //每天上传图片阈值
    private Integer dayImageUploadCount;
    //上传附件大小阈值
    private Integer attachmentSize;

    public Integer getPostIntegral() {
        return postIntegral;
    }

    public void setPostIntegral(Integer postIntegral) {
        this.postIntegral = postIntegral;
    }

    public Integer getPostDayCountThreshold() {
        return postDayCountThreshold;
    }

    public void setPostDayCountThreshold(Integer postDayCountThreshold) {
        this.postDayCountThreshold = postDayCountThreshold;
    }

    public Integer getDayImageUploadCount() {
        return dayImageUploadCount;
    }

    public void setDayImageUploadCount(Integer dayImageUploadCount) {
        this.dayImageUploadCount = dayImageUploadCount;
    }

    public Integer getAttachmentSize() {
        return attachmentSize;
    }

    public void setAttachmentSize(Integer attachmentSize) {
        this.attachmentSize = attachmentSize;
    }
}
