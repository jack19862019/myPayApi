package com.pay.rmi.paythird.quantong.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.quantong.QuanTongBackHelper;
import com.pay.rmi.paythird.quantong.QuanTongOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 全通
 */
@Service(QuanTong.channelNo)
public class QuanTong extends OrderApiFactory implements PayService {

    static final String channelNo = "quantong";

    @Autowired
    QuanTongOrderHelper quanTongOrderHelper;

    @Autowired
    QuanTongBackHelper quanTongBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        quanTongOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = quanTongOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(params);
        String hmac = quanTongOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("hmac", hmac);

        if ("aliwap".equals(reqParams.getOutChannel()) || "wechatwap".equals(reqParams.getOutChannel())
                ||"alih5".equals(reqParams.getOutChannel()) || "wechath5".equals(reqParams.getOutChannel())
                || "unionquickpay".equals(reqParams.getOutChannel())){
            //from表单提交
            return quanTongOrderHelper.returnDown(channel.getUpPayUrl());
        }else {
            //请求支付
            String result = quanTongOrderHelper.httpPost(map);
            //响应下游
            return quanTongOrderHelper.returnDown(result);
        }
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        QuanTongBackHelper qtJiBack = quanTongBackHelper.init(mcpConfig, order, params);
        return qtJiBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
