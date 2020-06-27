package com.pay.rmi.paythird.weihu.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.weihu.WeiHuBackHelper;
import com.pay.rmi.paythird.weihu.WeiHuOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 威虎
 */
@Service(WeiHu.channelNo)
public class WeiHu extends OrderApiFactory implements PayService {

    static final String channelNo = "weihu";

    @Autowired
    WeiHuOrderHelper weiHuOrderHelper;

    @Autowired
    WeiHuBackHelper weiHuBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        weiHuOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = weiHuOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(map);
        String sign = weiHuOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //请求支付
        String result = weiHuOrderHelper.httpPost(map);
        //响应下游
        return weiHuOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        WeiHuBackHelper weiHuBack = weiHuBackHelper.init(mcpConfig, order, params);
        return weiHuBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
