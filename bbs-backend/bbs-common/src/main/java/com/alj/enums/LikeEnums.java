package com.alj.enums;

public enum LikeEnums {
    ARTICLE_LIKE(0,"文章点赞"),
    COMMENT_LIKE(1,"评论点赞");
    private Integer type;
    public String desc;

    LikeEnums(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
