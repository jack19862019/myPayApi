package com.pay.rmi.paythird.mogu.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.mogu.MoGuBackHelper;
import com.pay.rmi.paythird.mogu.MoGuOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 蘑菇
 */
@Service(MoGuYL.channelNo)
public class MoGuYL extends OrderApiFactory implements PayService {

    static final String channelNo = "moguylgj";

    @Autowired
    MoGuOrderHelper moGuOrderHelper;

    @Autowired
    MoGuBackHelper moGuBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        moGuOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = moGuOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(map);
        String sign = moGuOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //响应下游
        return moGuOrderHelper.returnDown(channel.getUpPayUrl());
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        MoGuBackHelper moGuBack = moGuBackHelper.init(mcpConfig, order, params);
        return moGuBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
