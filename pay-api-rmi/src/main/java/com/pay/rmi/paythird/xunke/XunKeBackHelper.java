package com.pay.rmi.paythird.xunke;

import com.pay.common.enums.OrderStatus;
import com.pay.common.exception.Assert;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.paythird.CallBackFactory;
import com.pay.rmi.paythird.xunke.util.EkaPayEncrypt;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

@Component
public class XunKeBackHelper extends CallBackFactory {

    private String flagSuccess = "opstate=0";

    public XunKeBackHelper init(McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        this.mcpConfig = mcpConfig;
        this.callBackParams = params;
        this.order = order;
        return this;
    }

    public XunKeBackHelper checkOrder() {
        Assert.mustBeTrue(order.getOrderStatus() != OrderStatus.succ, order.getOrderNo() + "订单重复回调！");
        return this;
    }

    public XunKeBackHelper verifySign() {
        Map<String, String> treeMap = new TreeMap<>(callBackParams);
        String sign = treeMap.remove("sign");

        String orderid = treeMap.get("orderid");
        String opstate = treeMap.get("opstate");
        String ovalue = treeMap.get("ovalue");
        String newSign = EkaPayEncrypt.EkaPayCardBackMd5Sign(orderid,opstate,ovalue,mcpConfig.getUpKey());//签名
        Assert.mustBeTrue(newSign.equals(sign), order.getOrderNo() + "订单验签失败！");
        return this;
    }

    public XunKeBackHelper checkStatus() {
        String tradeStatus = callBackParams.get("opstate");
        Assert.mustBeTrue("0".equals(tradeStatus), "支付未成功，终止通知下游！");
        return this;
    }

    public XunKeBackHelper updateOrder() {
        String trade_no = callBackParams.get("orderid");
        String amount = callBackParams.get("ovalue");
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
