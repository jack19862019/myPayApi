package com.pay.rmi.paythird.haipingxian.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.haipingxian.HaiPingXianBackHelper;
import com.pay.rmi.paythird.haipingxian.HaiPingXianOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 海平线
 */
@Service(HaiPingXian.channelNo)
public class HaiPingXian extends OrderApiFactory implements PayService {

    static final String channelNo = "haipingxian";

    @Autowired
    HaiPingXianOrderHelper haiPingXianOrderHelper;

    @Autowired
    HaiPingXianBackHelper haiPingXianBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        haiPingXianOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = haiPingXianOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(params);
        String sign = haiPingXianOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //请求支付
        String result = haiPingXianOrderHelper.httpPost(map);
        //响应下游
        return haiPingXianOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        HaiPingXianBackHelper haiPingXianBack = haiPingXianBackHelper.init(mcpConfig, order, params);
        return haiPingXianBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
