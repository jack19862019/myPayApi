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

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OrderApiFactory {

    @Autowired
    protected NotifyTask notifyTask;

    @Value("${platform.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    protected ChannelEntity channel;
    protected McpConfigEntity mcpConfig;
    protected OrderReqParams reqParams;
    protected OrderEntity order;

    protected String result;

    protected Map<String, String> params = new HashMap<>();

    protected String getCallbackUrl() {
        return domain + contextPath + "/callback/" + reqParams.getChannelNo() + "/" + reqParams.getMerchNo() + "/" + reqParams.getOrderNo();
    }

    protected OrderApiFactory() {
    }

    protected void saveOrder(OrderReqParams reqParams, String upMerchantNo) {
        OrderEntity order = BeanCopyUtils.copyBean(reqParams, OrderEntity.class);
        order.setChannel(channelRepository.findByChannelFlag(reqParams.getChannelNo()));
        order.setMerchant(merchantRepository.findByMerchantNo(reqParams.getMerchNo()));
        order.setOrderAmount(new BigDecimal(reqParams.getAmount()));
        order.setUpMerchantNo(upMerchantNo);
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
    protected ApiOrderService orderService;
}
