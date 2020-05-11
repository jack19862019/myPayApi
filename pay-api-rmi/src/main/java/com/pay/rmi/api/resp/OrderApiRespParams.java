package com.pay.rmi.api.resp;

import lombok.Data;

@Data
public class OrderApiRespParams {

    private String orderNo;

    private String outChannel;

    private String merchNo;

    private String channelNo;

    private String amount;

    private String pay_form;

    private String qrcode_url;

    private String code_url;
}
