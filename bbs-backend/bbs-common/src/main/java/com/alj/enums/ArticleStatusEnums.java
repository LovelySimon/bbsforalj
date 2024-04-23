package com.alj.enums;

public enum ArticleStatusEnums {
    DEL(-1,"已删除"),
    NO_AUDIT(0,"待审核"),
    AUDIT(1,"已审核");

    private Integer status;
    private String desc;

    public static ArticleStatusEnums getByStatus(Integer status){
        for(ArticleStatusEnums item: ArticleStatusEnums.values()){
            if (item.getStatus().equals(status)){
                return item;
            }
        }
        return null;
    }

    ArticleStatusEnums(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
