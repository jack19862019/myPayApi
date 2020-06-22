package com.pay.rmi.paythird.heji.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.heji.HeJiBackHelper;
import com.pay.rmi.paythird.heji.HeJiOrderHelper;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 和记
 */
@Service(Heji.channelNo)
public class Heji extends OrderApiFactory implements PayService {

    static final String channelNo = "heji";

    @Autowired
    HeJiOrderHelper heJiOrderHelper;

    @Autowired
    HeJiBackHelper heJiBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        heJiOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = heJiOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = StrKit.formatSignData(map);
        String sign = heJiOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //请求支付
        String result = heJiOrderHelper.httpPost(map);
        //响应下游
        return heJiOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        HeJiBackHelper heJiBack = heJiBackHelper.init(mcpConfig, order, params);
        return heJiBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
