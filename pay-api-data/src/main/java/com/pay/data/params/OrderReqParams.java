package com.pay.data.params;

import com.pay.common.annotation.Remark;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class OrderReqParams {

    @Remark(name = "商户号")
    @NotBlank(message = "商户号不能为空")
    private String merchNo;

    @Remark(name = "订单号")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @Remark(name = "订单金额")
    @NotBlank(message = "金额不能为空")
    private String amount;

    @Remark(name = "金额币种")
    private String currency = "RMB";

    @Remark(name = "支付方式")
    @NotBlank(message = "支付方式不能为空")
    private String outChannel;

    @Remark(name = "通道标识")
    @NotBlank(message = "通道标识不能为空")
    private String channelNo;

    @Remark(name = "产品")
    private String product="666";

    @Remark(name = "描述")
    private String memo;

    @Remark(name = "同步地址")
    @NotBlank(message = "同步地址不能为空")
    private String returnUrl;

    @Remark(name = "异步地址")
    @NotBlank(message = "异步回调地址不能为空")
    private String notifyUrl;

    @Remark(name = "请求时间")
    @NotNull(message = "请求时间不能为空")
    private Date reqTime;

    @Remark(name = "用户ID")
    private String userId;

    @Remark(name = "请求IP")
    private String reqIp;

    //代付参数
    @Remark(name = "银行编码")
    private String bankCode;

    @Remark(name = "银行账号名称")
    private String bankAccountName;

    @Remark(name = "银行账号")
    private String bankAccountNo;


}
