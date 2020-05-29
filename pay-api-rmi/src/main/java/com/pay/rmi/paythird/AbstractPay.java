package com.pay.rmi.paythird;

import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.mapper.ChannelRepository;
import com.pay.data.mapper.MerchantRepository;
import com.pay.data.mapper.PayTypeRepository;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.pay.order.delay.NotifyTask;
import com.pay.rmi.service.ApiOrderService;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public abstract class AbstractPay implements PayService {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${platform.domain}")
    private String domain;

    protected ChannelEntity channelEntity;

    protected McpConfigEntity mcpConfig;

    protected OrderReqParams reqParams;

    protected OrderEntity order;

    @Autowired
    protected NotifyTask notifyTask;


    //========================================华丽得分割线=======================================================================

    protected void initReqOrder(ChannelEntity channel, McpConfigEntity mcpConfig, OrderReqParams reqParams){
        this.channelEntity = channel;
        this.mcpConfig = mcpConfig;
        this.reqParams = reqParams;
    }

    protected abstract Map<String, String> requestToUpParams();

    protected abstract String signToUp(String context);

    protected abstract String httpRequestToUp(String payUrl, Map<String, String> requestToUpParams);

    protected abstract OrderApiRespParams returnRespToDown(String result);

    //========================================华丽得分割线=======================================================================
    protected void initCallBack(OrderEntity orderEntity,McpConfigEntity mcpConfig){
        this.order = orderEntity;
        this.mcpConfig = mcpConfig;
    }

    protected abstract boolean verifySignParams(Map<String, String> params);

    protected abstract String updateOrder(Map<String, String> params);

    protected String getCallbackUrl(String channelNo, String merchantNo, String orderNo) {
        return domain + contextPath + "/callback/" + channelNo + "/" + merchantNo + "/" + orderNo;
    }


    protected void saveOrder(OrderReqParams reqParams, String upMerchantNo) {
        saveOrder(reqParams, upMerchantNo, null);
    }

    private void saveOrder(OrderReqParams reqParams, String upMerchantNo, String businessNo) {
        OrderEntity order = BeanCopyUtils.copyBean(reqParams, OrderEntity.class);
        order.setChannel(channelRepository.findByChannelFlag(reqParams.getChannelNo()));
        order.setMerchant(merchantRepository.findByMerchantNo(reqParams.getMerchNo()));
        order.setOrderAmount(new BigDecimal(reqParams.getAmount()));
        order.setUpMerchantNo(upMerchantNo);
        order.setBusinessNo(businessNo);
        order.setCreateTime(new Date());
        order.setCreateUser(reqParams.getMerchNo());
        order.setUpdateTime(new Date());
        order.setUpdateUser(reqParams.getMerchNo());
        orderService.save(order);
    }

    @Autowired
    PayTypeRepository payTypeRepository;

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    MerchantRepository merchantRepository;

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected ApiOrderService orderService;
}
