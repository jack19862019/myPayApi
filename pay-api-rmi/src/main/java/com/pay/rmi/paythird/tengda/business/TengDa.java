package com.pay.rmi.paythird.tengda.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.tengda.TengDaBackHelper;
import com.pay.rmi.paythird.tengda.TengDaOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 腾达
 */
@Service(TengDa.channelNo)
public class TengDa extends OrderApiFactory implements PayService {

    static final String channelNo = "tengda";

    @Autowired
    TengDaOrderHelper tengDaOrderHelper;

    @Autowired
    TengDaBackHelper tengDaBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        tengDaOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = tengDaOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(map);
        String sign = tengDaOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //from表单提交
        return tengDaOrderHelper.returnDown(channel.getUpPayUrl());
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        TengDaBackHelper tengDaBack = tengDaBackHelper.init(mcpConfig, order, params);
        return tengDaBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
