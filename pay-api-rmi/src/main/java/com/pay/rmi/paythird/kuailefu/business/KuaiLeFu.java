package com.pay.rmi.paythird.kuailefu.business;

import com.pay.common.enums.OrderStatus;
import com.pay.common.exception.Assert;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.paythird.AbstractPay;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.kuailefu.*;
import com.pay.rmi.paythird.kuailefu.util.PayMD5;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 快乐付
 */
@Service(KuaiLeFu.channelNo)
public class KuaiLeFu extends AbstractPay implements PayService {

    static final String channelNo = "kuailefu";

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //加签参数Map
        String callbackUrl = getCallbackUrl(channelNo, reqParams.getMerchNo(), reqParams.getOrderNo());
        Map<String, String> requestToUpParams = reqParamsHelper.requestToUpParams(channel, mcpConfig, reqParams, callbackUrl);
        //签名
        String signStr = StrKit.formatSignData(requestToUpParams);
        String sign = signHelper.signToUp(signStr, mcpConfig.getUpKey());
        requestToUpParams.put("sign", sign);
        //请求上游支付地址
        String result = httpReqHelper.httpRequestToUp(channel.getUpPayUrl(), requestToUpParams);
        //处理请求
        OrderApiRespParams orderApiRespParams = BeanCopyUtils.copyBean(reqParams, OrderApiRespParams.class);
        orderApiRespParams = returnDownHelper.returnDown(result, orderApiRespParams);
        //保存订单
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        return orderApiRespParams;
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        if (order.getOrderStatus() == OrderStatus.succ) {
            return "SUCCESS";
        }
        verifySignHelper.verifySign(params, mcpConfig.getUpKey());
        return null;// updateOrder(params);
    }

    /*public String updateOrder(Map<String, String> params) {
        String trade_no = params.get("orderNo");
        String trade_status = params.get("status");
        String amount = params.get("bizAmt");

        if (!"1".equals(trade_status)) {
            return "SUCCESS";
        }

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
        return "SUCCESS";
    }*/

    @Autowired
    ReqParamsHelper reqParamsHelper;

    @Autowired
    SignHelper signHelper;

    @Autowired
    HttpReqHelper httpReqHelper;

    @Autowired
    ReturnDownHelper returnDownHelper;

    @Autowired
    VerifySignHelper verifySignHelper;
}
