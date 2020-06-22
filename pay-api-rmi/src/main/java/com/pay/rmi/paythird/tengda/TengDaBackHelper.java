package com.pay.rmi.paythird.tengda;

import com.pay.common.enums.OrderStatus;
import com.pay.common.exception.Assert;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.paythird.CallBackFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Component
public class TengDaBackHelper extends CallBackFactory {

    private String flagSuccess = "success";

    public TengDaBackHelper init(McpConfigEntity mcpConfig, OrderEntity order, Map<String, String> params) {
        this.mcpConfig = mcpConfig;
        this.callBackParams = params;
        this.order = order;
        return this;
    }

    public TengDaBackHelper checkOrder() {
        Assert.mustBeTrue(order.getOrderStatus() != OrderStatus.succ, order.getOrderNo() + "订单重复回调！");
        return this;
    }

    public TengDaBackHelper verifySign() {
        Map<String, String> treeMap = new TreeMap<>(callBackParams);
        String sign = treeMap.remove("sign");
        String status = treeMap.get("status");
        String shid = treeMap.get("shid");
        String zftd = treeMap.get("zftd");
        String ddh = treeMap.get("ddh");
        String je = treeMap.get("je");
        String ybtz = treeMap.get("ybtz");
        String tbtz = treeMap.get("tbtz");
        String buildParams ="status="+status+"&shid="+shid+"&bb=1.0&zftd="+zftd+"&ddh="+ddh+"&je="+je+"&ddmc=666&ddbz=666&ybtz="+ybtz+"&tbtz="+tbtz+"&"+mcpConfig.getUpKey();
        String newSign = Objects.requireNonNull(Md5Utils.MD5(buildParams));
        Assert.mustBeTrue(newSign.equals(sign), order.getOrderNo() + "订单验签失败！");
        return this;
    }

    public TengDaBackHelper checkStatus() {
        String tradeStatus = callBackParams.get("status");
        Assert.mustBeTrue("success".equals(tradeStatus), "支付未成功，终止通知下游！");
        return this;
    }

    public TengDaBackHelper updateOrder() {
        String trade_no = callBackParams.get("ddh");
        String amount = callBackParams.get("je");
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
