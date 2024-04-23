package com.alj.util;

import lombok.Data;

@Data
public class BaseResult {
    private int code;
    private String msg;
    private Object obj;

    public BaseResult(int code, String msg, Object obj) {
        this.code = code;
        this.msg = msg;
        this.obj = obj;
    }

    private BaseResult() {

    }

    public static BaseResult getBaseResult(int code, String msg, Object obj) {
        return new BaseResult(code, msg, obj);
    }
}
