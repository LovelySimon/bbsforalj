package com.alj.enums;

public enum MessageStatus {
    NO_READ(1,"未读"),
    READ(0,"已读");


    private Integer status;
    private String desc;

    MessageStatus(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static MessageStatus getBystatus(Integer status){
        for (MessageStatus item:MessageStatus.values()){
            if (item.getStatus().equals(status)){
                return item;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
