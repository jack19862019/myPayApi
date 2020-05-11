package com.pay.manager.pc.order.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class OrderPageReqParams {

    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    @NotBlank(message = "商户编号不能为空")
    private String merchantNo;

    @NotBlank(message = "商户名称不能为空")
    private String merchantName;

    @NotBlank(message = "通道名称不能为空")
    private String channelName;

    @NotBlank(message = "支付方式不能为空")
    private String payTypeName;

    @NotBlank(message = "订单金额不能为空")
    private BigDecimal orderAmount;

    private BigDecimal realAmount;

    private String businessNo;
}
