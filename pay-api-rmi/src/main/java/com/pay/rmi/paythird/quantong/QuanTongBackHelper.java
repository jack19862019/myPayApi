package com.pay.rmi.paythird.quantong;

import com.pay.common.enums.OrderStatus;
import com.pay.common.exception.Assert;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.paythird.CallBackFactory;
import com.pay.rmi.paythird.quantong.util.DigestUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

@Component
public class QuanTongBackHelper extends CallBackFactory {

    private String flagSuccess = "success";

    public QuanTongBackHelper init(McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        this.mcpConfig = mcpConfig;
        this.callBackParams = params;
        this.order = order;
        return this;
    }

    public QuanTongBackHelper checkOrder() {
        Assert.mustBeTrue(order.getOrderStatus() != OrderStatus.succ, order.getOrderNo() + "订单重复回调！");
        return this;
    }

    public QuanTongBackHelper verifySign() {
        Map<String, String> params = new TreeMap<>(callBackParams);
        String sign = params.remove("hmac");
        String signParams = params.get("p1_MerId")+params.get("r0_Cmd")+params.get("r1_Code")+params.get("r2_TrxId")+
                params.get("r3_Amt")+params.get("r4_Cur")+params.get("r5_Pid")+params.get("r6_Order")+
                params.get("r7_Uid")+params.get("r8_MP")+params.get("r9_BType");
        String newSign = DigestUtil.hmacSign(signParams, mcpConfig.getUpKey());
        Assert.mustBeTrue(newSign.equals(sign), order.getOrderNo() + "订单验签失败！");
        return this;
    }

    public QuanTongBackHelper checkStatus() {
        String tradeStatus = callBackParams.get("r1_Code");
        Assert.mustBeTrue("1".equals(tradeStatus), "支付未成功，终止通知下游！");
        return this;
    }

    public QuanTongBackHelper updateOrder() {
        String trade_no = callBackParams.get("r6_Order");
        String amount = callBackParams.get("r3_Amt");
        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount));
        order.setBusinessNo(trade_no);
        orderService.update(order);
        //通知下游
        try {
            notifyTask.put(order);
        } catch (Exception e) {
            throw new RException("下发通知报错:" + e.getMessage());
        }
        return this;
    }

    public String done() {
        return flagSuccess;
    }

}
