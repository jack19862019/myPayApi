package com.pay.rmi.paythird.xincheng.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import com.pay.rmi.paythird.xincheng.XinChengBackHelper;
import com.pay.rmi.paythird.xincheng.XinChengOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 新城
 */
@Service(XinCheng.channelNo)
public class XinCheng extends OrderApiFactory implements PayService {

    static final String channelNo = "xincheng";

    @Autowired
    XinChengOrderHelper xinChengOrderHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        xinChengOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = xinChengOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = StrKit.formatSignData(map);
        String sign = xinChengOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("Sign", sign);
        //from表单提交
        return xinChengOrderHelper.returnDown(channel.getUpPayUrl());
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        XinChengBackHelper xinChengBackHelper= new XinChengBackHelper(mcpConfig, order, params);
        return xinChengBackHelper.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
