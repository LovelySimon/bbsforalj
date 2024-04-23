package com.alj.enums;

public enum CommentTopEnums {
    NO_TOP(0,"未置顶"),
    TOP(1,"已置顶");

    private Integer type;
    private String desc;

    CommentTopEnums(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static CommentTopEnums getByType(Integer type){
        for(CommentTopEnums item: CommentTopEnums.values()){
            if (item.getType().equals(type)){
                return item;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
