package com.alj.enums;

public enum UserIntegralOpertype {
    REGISTER(1,"账号注册"),
    POST_COMMENT(2,"发布评论"),
    POST_ARTICLE(3,"发布文章"),
    ADMIN(4,"管理员操作"),
    DEL_ARTICLE(5,"文章被删除"),
    DEL_COMMENT(6,"评论被删除");



    private Integer operType;
    private String desc;

    UserIntegralOpertype(Integer operType, String desc) {
        this.operType = operType;
        this.desc = desc;
    }
    public static UserIntegralOpertype getBytype(Integer operType){
        for (UserIntegralOpertype item:UserIntegralOpertype.values()){
            if (item.getOperType().equals(operType)){
                return item;
            }
        }
        return null;
    }

    public Integer getOperType() {
        return operType;
    }

    public String getDesc() {
        return desc;
    }
}
