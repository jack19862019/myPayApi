package com.pay.rmi.paythird.katong.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.katong.KaTongBackHelper;
import com.pay.rmi.paythird.katong.KaTongOrderHelper;
import com.pay.rmi.paythird.katong.util.KaTongUtil;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 卡通
 */
@Service(KaTong.channelNo)
public class KaTong extends OrderApiFactory implements PayService {

    static final String channelNo = "katong";

    @Autowired
    KaTongOrderHelper kaTongOrderHelper;

    @Autowired
    KaTongBackHelper kaTongBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        kaTongOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = kaTongOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = KaTongUtil.generateSignReduce(map);
        String sign = kaTongOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        //请求支付
        String result = channel.getUpPayUrl()+signData+ "&sign=" + sign;
        //响应下游
        saveOrder(reqParams, mcpConfig.getUpMerchantNo());
        return kaTongOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        KaTongBackHelper kaTongBack = kaTongBackHelper.init(mcpConfig, order, params);
        return kaTongBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
