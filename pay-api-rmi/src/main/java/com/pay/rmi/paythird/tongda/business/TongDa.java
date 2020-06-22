package com.pay.rmi.paythird.tongda.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.tongda.TongDaBackHelper;
import com.pay.rmi.paythird.tongda.TongDaOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 通达
 */
@Service(TongDa.channelNo)
public class TongDa extends OrderApiFactory implements PayService {

    static final String channelNo = "tongda";

    @Autowired
    TongDaOrderHelper tongDaOrderHelper;

    @Autowired
    TongDaBackHelper tongDaBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        tongDaOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = tongDaOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(params);
        String sign = tongDaOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //请求支付
        String result = tongDaOrderHelper.httpPost(map);
        //响应下游
        return tongDaOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        TongDaBackHelper tongDaBack = tongDaBackHelper.init(mcpConfig, order, params);
        return tongDaBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
