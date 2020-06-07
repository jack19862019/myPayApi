package com.pay.rmi.paythird.xincheng;

import com.pay.common.enums.OrderStatus;
import com.pay.common.exception.Assert;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.pay.order.delay.NotifyTask;
import com.pay.rmi.paythird.CallBackFactory;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import com.pay.rmi.service.ApiOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

@Component
public class XinChengBackHelper extends CallBackFactory {

    private String flagSuccess = "success";

    public XinChengBackHelper init(McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        this.mcpConfig = mcpConfig;
        this.callBackParams = params;
        this.order = order;
        return this;
    }

    public XinChengBackHelper checkOrder() {
        Assert.mustBeTrue(order.getOrderStatus() != OrderStatus.succ, order.getOrderNo() + "订单重复回调！");
        return this;
    }

    public XinChengBackHelper verifySign() {
        Map<String, String> treeMap = new TreeMap<>(callBackParams);
        String sign = treeMap.remove("Sign");
        String signStr = StrKit.formatSignData(callBackParams);
        String newSign = PayMD5.MD5Encode(signStr +"&key="+ mcpConfig.getUpKey()).toUpperCase();
        Assert.mustBeTrue(newSign.equals(sign), order.getOrderNo() + "订单验签失败！");
        return this;
    }

    public XinChengBackHelper checkStatus() {
        String tradeStatus = callBackParams.get("PayStatus");
        Assert.mustBeTrue("Order_SUCCESS".equals(tradeStatus), "支付未成功，终止通知下游！");
        return this;
    }

    public XinChengBackHelper updateOrder() {
        String trade_no = callBackParams.get("CreateTradeNo");
        String amount = callBackParams.get("CreateMoney");
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
