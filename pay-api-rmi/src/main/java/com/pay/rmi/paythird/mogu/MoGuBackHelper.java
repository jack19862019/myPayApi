package com.pay.rmi.paythird.mogu;

import com.pay.common.enums.OrderStatus;
import com.pay.common.exception.Assert;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.paythird.CallBackFactory;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

@Component
public class MoGuBackHelper extends CallBackFactory {

    private String flagSuccess = "SUCCESS";

    public MoGuBackHelper init(McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        this.mcpConfig = mcpConfig;
        this.callBackParams = params;
        this.order = order;
        return this;
    }

    public MoGuBackHelper checkOrder() {
        Assert.mustBeTrue(order.getOrderStatus() != OrderStatus.succ, order.getOrderNo() + "订单重复回调！");
        return this;
    }

    public MoGuBackHelper verifySign() {
        Map<String, String> treeMap = new TreeMap<>(callBackParams);
        String sign = treeMap.remove("sign");
        String signStr = StrKit.formatSignData(callBackParams);
        String newSign = PayMD5.MD5Encode(signStr + mcpConfig.getUpKey()).toLowerCase();
        Assert.mustBeTrue(newSign.equals(sign), order.getOrderNo() + "订单验签失败！");
        return this;
    }

    public MoGuBackHelper checkStatus() {
        String tradeStatus = callBackParams.get("code");
        Assert.mustBeTrue("0000".equals(tradeStatus), "支付未成功，终止通知下游！");
        return this;
    }

    public MoGuBackHelper updateOrder() {
        String trade_no = callBackParams.get("out_trade_no");
        String amount = callBackParams.get("amount");
        order.setOrderStatus(OrderStatus.succ);
        order.setRealAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));
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
