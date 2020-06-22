package com.pay.rmi.paythird.xiami.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.xiami.XiaMiBackHelper;
import com.pay.rmi.paythird.xiami.XiaMiOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 虾米
 */
@Service(XiaMi.channelNo)
public class XiaMi extends OrderApiFactory implements PayService {

    static final String channelNo = "xiami";

    @Autowired
    XiaMiOrderHelper xiaMiOrderHelper;

    @Autowired
    XiaMiBackHelper xiaMiBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        xiaMiOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = xiaMiOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(params);
        String sign = xiaMiOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("sign", sign);
        //请求支付
        String result = xiaMiOrderHelper.httpPost(map);
        //响应下游
        return xiaMiOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        XiaMiBackHelper xiaMiBack = xiaMiBackHelper.init(mcpConfig, order, params);
        return xiaMiBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
