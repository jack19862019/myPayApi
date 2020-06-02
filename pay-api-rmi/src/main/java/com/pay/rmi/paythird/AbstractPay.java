package com.pay.rmi.paythird;

import com.pay.data.entity.OrderEntity;
import com.pay.data.mapper.ChannelRepository;
import com.pay.data.mapper.MerchantRepository;
import com.pay.data.mapper.PayTypeRepository;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.pay.order.delay.NotifyTask;
import com.pay.rmi.service.ApiOrderService;
import com.tuyang.beanutils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Date;

public abstract class AbstractPay {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${platform.domain}")
    private String domain;

    @Autowired
    protected NotifyTask notifyTask;


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
