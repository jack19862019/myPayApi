package com.pay.rmi.paythird.xunke.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.xunke.XunKeBackHelper;
import com.pay.rmi.paythird.xunke.XunKeOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 讯科
 */
@Service(XunKe.channelNo)
public class XunKe extends OrderApiFactory implements PayService {

    static final String channelNo = "xunke";

    @Autowired
    XunKeOrderHelper xunKeOrderHelper;

    @Autowired
    XunKeBackHelper xunKeBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        xunKeOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = xunKeOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(map);
        String sign = xunKeOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //请求支付
        String result = xunKeOrderHelper.httpPost(map);
        //响应下游
        return xunKeOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        XunKeBackHelper xunKeBack = xunKeBackHelper.init(mcpConfig, order, params);
        return xunKeBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
