package com.pay.rmi.paythird.niuniu.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.niuniu.NiuNiuBackHelper;
import com.pay.rmi.paythird.niuniu.NiuNiuOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 牛牛
 */
@Service(NiuNiu.channelNo)
public class NiuNiu extends OrderApiFactory implements PayService {

    static final String channelNo = "niuniu";

    @Autowired
    NiuNiuOrderHelper niuNiuOrderHelper;

    @Autowired
    NiuNiuBackHelper niuNiuBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        niuNiuOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = niuNiuOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(map);
        String sign = niuNiuOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //请求支付
        String result = niuNiuOrderHelper.httpPost(map);
        //响应下游
        return niuNiuOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        NiuNiuBackHelper niuNiuBack = niuNiuBackHelper.init(mcpConfig, order, params);
        return niuNiuBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
