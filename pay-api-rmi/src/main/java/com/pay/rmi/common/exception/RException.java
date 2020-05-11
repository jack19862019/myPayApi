package com.pay.rmi.common.exception;

public class RException extends RuntimeException {

    private int code = 500;

    private String channelNo;

    private String msg;

    public RException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public RException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public RException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    /**
     * 用于邮件报警
     * @param channelNo 上有通道标识
     * @param msg 报错信息
     * @param e 报错信息栈
     */
    public RException(String channelNo, String msg, Throwable e) {
        super(msg, e);
        this.channelNo = channelNo;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getchannelNo() {
        return channelNo;
    }

    public void setchannelNo(String channelNo) {
        this.channelNo = channelNo;
    }
}
