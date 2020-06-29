package com.pay.rmi.paythird.jialian.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.jialian.JiaLianBackHelper;
import com.pay.rmi.paythird.jialian.JiaLianOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 嘉联
 */
@Service(JiaLian.channelNo)
public class JiaLian extends OrderApiFactory implements PayService {

    static final String channelNo = "jialian";

    @Autowired
    JiaLianOrderHelper jiaLianOrderHelper;

    @Autowired
    JiaLianBackHelper jiaLianBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        jiaLianOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = jiaLianOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(map);
        String sign = jiaLianOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //from表单提交
        return jiaLianOrderHelper.returnDown(channel.getUpPayUrl());
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        JiaLianBackHelper jiaLianBack = jiaLianBackHelper.init(mcpConfig, order, params);
        return jiaLianBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
