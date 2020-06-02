package com.pay.data.params;

import com.pay.common.annotation.Remark;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class OrderReqParams {

    @NotBlank(message = "商户号不能为空")
    private String merchNo;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotBlank(message = "金额不能为空")
    private String amount;

    private String currency = "RMB";

    @NotBlank(message = "支付方式不能为空")
    private String outChannel;

    @NotBlank(message = "通道标识不能为空")
    private String channelNo;

    private String product="666";

    private String memo;

    @NotBlank(message = "同步地址不能为空")
    private String returnUrl;

    @NotBlank(message = "异步回调地址不能为空")
    private String notifyUrl;

    @NotNull(message = "请求时间不能为空")
    private Date reqTime;

    private String userId;

    private String reqIp;

    //代付参数
    private String bankCode;

    private String bankAccountName;

    private String bankAccountNo;


}
