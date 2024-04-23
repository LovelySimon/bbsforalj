package com.alj.dto;
/**
 * @description: 评论控制
 * @author alj
 * @date 2024/4/2 14:31
 * @version 1.0
 */

public class SysSettingComment {
    //阈值
    private Integer commentDayCountThreshold;
    //评论积分
    private Integer commentIntegral;
    //评论是否开启
    private Boolean commentOpen;

    public Integer getCommentDayCountThreshold() {
        return commentDayCountThreshold;
    }

    public void setCommentDayCountThreshold(Integer commentDayCountThreshold) {
        this.commentDayCountThreshold = commentDayCountThreshold;
    }

    public Integer getCommentIntegral() {
        return commentIntegral;
    }

    public void setCommentIntegral(Integer commentIntegral) {
        this.commentIntegral = commentIntegral;
    }

    public Boolean getCommentOpen() {
        return commentOpen;
    }

    public void setCommentOpen(Boolean commentOpen) {
        this.commentOpen = commentOpen;
    }
}
