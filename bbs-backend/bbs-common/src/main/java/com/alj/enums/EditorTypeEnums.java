package com.alj.enums;

public enum EditorTypeEnums {
    RICH(0,"富文本"),
    MARKDOWN(1,"Markdown");


    private Integer type;
    private String desc;

    EditorTypeEnums(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static EditorTypeEnums getByType(Integer type){
        for (EditorTypeEnums item:EditorTypeEnums.values()){
            if (item.getType().equals(type)){
                return item;
            }
        }
        return null;
    }
}
