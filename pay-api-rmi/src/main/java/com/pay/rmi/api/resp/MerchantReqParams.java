package com.pay.rmi.api.resp;

import lombok.Data;

@Data
public class MerchantReqParams {

    private String merchNo;
    private String orderNo;
    private String amount;
    //默认
    private String currency = "RMB";
    private String outChannel;//支付方式
    private String channelNo;//通道标识
    private String product;
    private String memo;
    private String returnUrl;
    private String notifyUrl;
    private String reqTime;
    private String userId;
    private String reqIp;

    private String encryptType;

    //代付参数
    private String bankCode;
    private String bankAccountName;
    private String bankAccountNo;
}
