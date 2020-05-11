package com.pay.common.enums;


public enum ResultCode {

    ROLE_UN_KNOW(401, "权限不足"),
    USER_EXIST(2000, "用户已经存在"),
    BAD_REQUEST(400, "参数或者语法不对"),
    UNAUTHORIZED(-88, "登录认证失败"),
    LOGIN_ERROR(402, "登录失败，密码错误"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    OPERATE_ERROR(405, "操作失败，请求操作的资源不存在"),
    TIME_OUT(408, "请求超时"),
    SERVER_ERROR(500, "服务器内部错误");


    private int code;
    private String msg;


    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
