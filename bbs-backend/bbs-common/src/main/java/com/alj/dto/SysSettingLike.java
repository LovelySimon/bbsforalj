package com.alj.dto;

/**
 * @description: 点赞设置
 * @author alj
 * @date 2024/4/2 14:35
 * @version 1.0
 */
public class SysSettingLike {
    //点赞数量阈值
    private Integer likeDayCountThreshold;

    public Integer getLikeDayCountThreshold() {
        return likeDayCountThreshold;
    }

    public void setLikeDayCountThreshold(Integer likeDayCountThreshold) {
        this.likeDayCountThreshold = likeDayCountThreshold;
    }
}
