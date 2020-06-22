package com.pay.rmi.paythird.weihu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pay.common.enums.OrderStatus;
import com.pay.common.exception.Assert;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.CallBackFactory;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

@Component
public class WeiHuBackHelper extends CallBackFactory {

    private String flagSuccess = "SUCCESS";

    public WeiHuBackHelper init(McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        this.mcpConfig = mcpConfig;
        this.callBackParams = params;
        this.order = order;
        return this;
    }

    public WeiHuBackHelper checkOrder() {
        Assert.mustBeTrue(order.getOrderStatus() != OrderStatus.succ, order.getOrderNo() + "订单重复回调！");
        return this;
    }

    public WeiHuBackHelper verifySign() {
        Map<String, String> treeMap = new TreeMap<>(callBackParams);
        String sign = treeMap.remove("sign");
        String data = treeMap.get("data");
        Map<String, String> resultData = JSON.parseObject(data, new TypeReference<Map<String, String>>() {
        });
        //加签
        Map<String, String> paramSign = new TreeMap<>();
        paramSign.put("order_no", resultData.get("order_no"));
        paramSign.put("trade_no", resultData.get("trade_no"));
        paramSign.put("amount", resultData.get("amount"));
        paramSign.put("pay_amount", resultData.get("pay_amount"));
        paramSign.put("trade_code", resultData.get("trade_code"));
        paramSign.put("status", resultData.get("status"));
        paramSign.put("complete_time", resultData.get("complete_time"));

        paramSign.put("merchant_id", treeMap.get("merchant_id"));
        paramSign.put("response_time", treeMap.get("response_time"));
        paramSign.put("nonce_str", treeMap.get("nonce_str"));
        paramSign.put("version", treeMap.get("version"));

        String signParams = SignUtils.buildParamsIgnoreNull(paramSign) + "&key=" + mcpConfig.getUpKey();
        String newSign = PayMD5.MD5Encode(signParams).toUpperCase();
        Assert.mustBeTrue(newSign.equals(sign), order.getOrderNo() + "订单验签失败！");
        return this;
    }

    public WeiHuBackHelper checkStatus() {
        Map maps = (Map)JSON.parse(callBackParams.get("data"));
        String tradeStatus = (String)maps.get("status");
        Assert.mustBeTrue("1000".equals(tradeStatus), "支付未成功，终止通知下游！");
        return this;
    }

    public WeiHuBackHelper updateOrder() {
        Map maps = (Map)JSON.parse(callBackParams.get("data"));
        String trade_no = (String)maps.get("order_no");
        String amount = (String)maps.get("amount");
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
