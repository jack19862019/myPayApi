package com.pay.rmi.api.resp;


import lombok.Data;

@Data
public class OrderListResp {

    private String merchantName;

    private String orderNo;

    private String channelName;

    private String payTypeName;

    private String amount;

    private String status;

    private String orderTime;


}
