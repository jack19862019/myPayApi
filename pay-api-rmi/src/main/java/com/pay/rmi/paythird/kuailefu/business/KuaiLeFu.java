package com.pay.rmi.paythird.kuailefu.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.kuailefu.KuaiLeFuOrderHelper;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 快乐付
 */
@Service(KuaiLeFu.channelNo)
public class KuaiLeFu extends OrderApiFactory implements PayService {

    static final String channelNo = "kuailefu";

    @Autowired
    KuaiLeFuOrderHelper kuaiLeFuOrderHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        kuaiLeFuOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = kuaiLeFuOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = StrKit.formatSignData(map);
        String sign = kuaiLeFuOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //请求支付
        String result = kuaiLeFuOrderHelper.httpPost(map);
        //响应下游
        return kuaiLeFuOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        return null;
    }
}
