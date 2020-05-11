package com.pay.common.utils;

import com.pay.common.enums.ResultCode;
import lombok.Data;

/**
 * 返回数据
 */
@Data
public class Result<T> {
    private int code;
    private String desc;
    private T data;

    public Result() {
    }

    public Result(int code, String desc, T data) {
        this.code = code;
        this.desc = desc;
        this.data = data;
    }

    public static Result success(Object data) {
        Result result = new Result();
        result.setCode(200);
        result.setDesc("SUCCESS");
        result.setData(data);
        return result;
    }

    public static Result authFailure(ResultCode resultCode){
        Result result = new Result();
        result.setCode(resultCode.getCode());
        result.setDesc(resultCode.getMsg());
        return result;
    }

    public static Result success() {
        return success(null);
    }

    public static Result error(int code, String desc) {
        Result result = new Result();
        result.setCode(code);
        result.setDesc(desc);
        return result;
    }

    public static Result error(String desc) {
        Result result = new Result();
        result.setCode(500);
        result.setDesc(desc);
        return result;
    }
}
