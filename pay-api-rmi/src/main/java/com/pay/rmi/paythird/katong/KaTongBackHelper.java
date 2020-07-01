package com.pay.rmi.paythird.katong;

import com.pay.common.enums.OrderStatus;
import com.pay.common.exception.Assert;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.paythird.CallBackFactory;
import com.pay.rmi.paythird.katong.util.KaTongUtil;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

@Component
public class KaTongBackHelper extends CallBackFactory {

    private String flagSuccess = "success";

    public KaTongBackHelper init(McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        this.mcpConfig = mcpConfig;
        this.callBackParams = params;
        this.order = order;
        return this;
    }

    public KaTongBackHelper checkOrder() {
        Assert.mustBeTrue(order.getOrderStatus() != OrderStatus.succ, order.getOrderNo() + "订单重复回调！");
        return this;
    }

    public KaTongBackHelper verifySign() {
        Map<String, String> treeMap = new TreeMap<>(callBackParams);
        String sign = treeMap.remove("sign");
        String signParams = KaTongUtil.generateSignReduce(treeMap);
        String newSign =KaTongUtil.encodeMD5(signParams + "&key=" +  mcpConfig.getUpKey());
        Assert.mustBeTrue(newSign.equals(sign.toUpperCase()), order.getOrderNo() + "订单验签失败！");
        return this;
    }

    public KaTongBackHelper checkStatus() {
        String tradeStatus = callBackParams.get("orderStatus");
        Assert.mustBeTrue("50".equals(tradeStatus), "支付未成功，终止通知下游！");
        return this;
    }

    public KaTongBackHelper updateOrder() {
        String trade_no = callBackParams.get("orderNo");
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
