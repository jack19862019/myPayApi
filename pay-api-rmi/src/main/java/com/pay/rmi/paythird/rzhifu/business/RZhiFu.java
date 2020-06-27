package com.pay.rmi.paythird.rzhifu.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.rzhifu.RZhiFuBackHelper;
import com.pay.rmi.paythird.rzhifu.RZhiFuOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * r支付
 */
@Service(RZhiFu.channelNo)
public class RZhiFu extends OrderApiFactory implements PayService {

    static final String channelNo = "rzhifu";

    @Autowired
    RZhiFuOrderHelper rZhiFuOrderHelper;

    @Autowired
    RZhiFuBackHelper rZhiFuBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        rZhiFuOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = rZhiFuOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(map);
        String sign = rZhiFuOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //from表单提交
        return rZhiFuOrderHelper.returnDown(channel.getUpPayUrl());
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        RZhiFuBackHelper rZhiFuBack = rZhiFuBackHelper.init(mcpConfig, order, params);
        return rZhiFuBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
