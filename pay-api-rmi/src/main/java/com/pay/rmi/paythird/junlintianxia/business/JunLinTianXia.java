package com.pay.rmi.paythird.junlintianxia.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.junlintianxia.JunLinTianXiaBackHelper;
import com.pay.rmi.paythird.junlintianxia.JunLinTianXiaOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 君临天下
 */
@Service(JunLinTianXia.channelNo)
public class JunLinTianXia extends OrderApiFactory implements PayService {

    static final String channelNo = "junlintianxia";

    @Autowired
    JunLinTianXiaOrderHelper junLinTianXiaOrderHelper;

    @Autowired
    JunLinTianXiaBackHelper junLinTianXiaBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        junLinTianXiaOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = junLinTianXiaOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(params);
        String sign = junLinTianXiaOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //请求支付
        String result = junLinTianXiaOrderHelper.httpPost(map);
        //响应下游
        return junLinTianXiaOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        JunLinTianXiaBackHelper junLinTianXiaBack = junLinTianXiaBackHelper.init(mcpConfig, order, params);
        return junLinTianXiaBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
