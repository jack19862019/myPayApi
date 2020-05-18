package com.pay.rmi.service;


import com.pay.common.enums.OrderStatus;
import com.pay.common.exception.Assert;
import com.pay.data.entity.OrderEntity;
import com.pay.data.mapper.ChannelRepository;
import com.pay.data.mapper.MerchantRepository;
import com.pay.data.mapper.OrderRepository;
import com.pay.rmi.api.resp.OrderListResp;
import com.pay.rmi.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ApiOrderServiceImpl implements ApiOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public OrderEntity selectByMerchNoAndOrderNo(String merchNo, String orderNo) {
        return orderRepository.findByMerchant_MerchantNoAndOrderNo(merchNo, orderNo);
    }

    @Override
    public OrderEntity save(OrderEntity order) {
        return orderRepository.save(order);
    }

    @Override
    public OrderEntity update(OrderEntity order) {
        //判断realAmount和orderAmount是不是差距太大
        BigDecimal orderAmount = order.getOrderAmount();
        BigDecimal realAmount = order.getRealAmount();
        Assert.mustBeTrue(orderAmount.compareTo(realAmount) >= 0,
                "实际金额大于支付金额，请查看"
        );
        Assert.mustBeTrue(
                rate(orderAmount, realAmount).compareTo(new BigDecimal(0.05)) < 0,
                "订单金额与实际金额差距太大，请查看"
        );
        return orderRepository.save(order);
    }

    private static BigDecimal rate(BigDecimal orderAmount, BigDecimal realAmount) {
        BigDecimal multiply = orderAmount.subtract(realAmount);
        return multiply.divide(orderAmount);
    }


    @Override
    public Page<OrderListResp> selectByFlagAndOrderNo(String orderNo, String pageSize, String pageNum) {
        Sort reqTime = new Sort(Sort.Direction.DESC, "reqTime");
        Pageable pageRequest = new PageRequest(Integer.parseInt(pageNum), Integer.parseInt(pageSize), reqTime);

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderNo(orderNo);
        orderEntity.setMemo("测试备注");
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("orderNo", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<OrderEntity> example = Example.of(orderEntity, matcher);
        return orderRepository.findAll(example, pageRequest).map(this::toMerchantName);
    }

    private OrderListResp toMerchantName(OrderEntity orderEntity) {
        OrderListResp resp = new OrderListResp();
        resp.setAmount(orderEntity.getOrderAmount().toString());
        resp.setChannelName(channelDao.findByChannelFlag(orderEntity.getChannel().getChannelFlag()).getChannelName());
        resp.setMerchantName(merchantDao.findByMerchantNo(orderEntity.getMerchant().getMerchantNo()).getMerchantName());
        resp.setOrderNo(orderEntity.getOrderNo());
        resp.setOrderTime(DateUtil.getCurrentStr(orderEntity.getCreateTime()));
        resp.setStatus(orderEntity.getOrderStatus().equals(OrderStatus.succ) ? "回调成功" : "暂无回调");
        return resp;
    }

    @Autowired
    MerchantRepository merchantDao;

    @Autowired
    ChannelRepository channelDao;
}
