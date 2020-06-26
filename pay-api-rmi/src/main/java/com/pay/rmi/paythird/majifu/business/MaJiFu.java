package com.pay.rmi.paythird.majifu.business;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.utils.SignUtils;
import com.pay.rmi.paythird.OrderApiFactory;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.kuailefu.util.StrKit;
import com.pay.rmi.paythird.majifu.MaJiFuBackHelper;
import com.pay.rmi.paythird.majifu.MaJiFuOrderHelper;
import com.pay.rmi.paythird.xincheng.XinChengBackHelper;
import com.pay.rmi.paythird.xincheng.XinChengOrderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 新城
 */
@Service(MaJiFu.channelNo)
public class MaJiFu extends OrderApiFactory implements PayService {

    static final String channelNo = "majifu";

    @Autowired
    MaJiFuOrderHelper maJiFuOrderHelper;

    @Autowired
    MaJiFuBackHelper maJiFuBackHelper;

    @Override
    public OrderApiRespParams orderBusiness(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams) {
        //初始化
        maJiFuOrderHelper.init(channel,mcpConfig,reqParams);
        //请求基本参数
        Map<String, String> map = maJiFuOrderHelper.requestToUpParams(reqParams);
        //加签
        String signData = SignUtils.buildParams(map, true);
        String sign = maJiFuOrderHelper.signToUp(signData, mcpConfig.getUpKey());
        map.put("mac", sign);
        String result = maJiFuOrderHelper.httpPost(map);
        //from表单提交
        return maJiFuOrderHelper.returnDown(result);
    }

    @Override
    public String callback(OrderEntity order, McpConfigEntity mcpConfig, Map<String, String> params) {
        MaJiFuBackHelper maJiFuBack = maJiFuBackHelper.init(mcpConfig, order, params);
        return maJiFuBack.checkOrder().verifySign().checkStatus().updateOrder().done();
    }
}
