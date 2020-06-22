package com.pay.rmi.paythird.aryazhifu.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.aryazhifu.ARYAOrderHelper;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import com.pay.rmi.paythird.aryazhifu.ARYABackHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ARYA云支付
 */
@Service(ARYAZhiFu.channelNo)
public class ARYAZhiFu extends OrderApiFactory implements PayService {

    static final String channelNo = "aryazhifu";

    @Autowired
    ARYABackHelper aryaBackHelper;

    @Autowired
    ARYAOrderHelper aryaOrderHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        aryaOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = aryaOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = StrKit.formatSignData(map);
        String sign = aryaOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //请求支付
        String result = aryaOrderHelper.httpPost(map);
        //响应下游
        return aryaOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        ARYABackHelper aryaBank = aryaBackHelper.init(mcpConfig, order, params);
        return aryaBank.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
