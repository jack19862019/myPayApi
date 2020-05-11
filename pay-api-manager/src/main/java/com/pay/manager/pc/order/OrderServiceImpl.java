package com.pay.manager.pc.order;

import com.alibaba.fastjson.JSON;
import com.pay.common.constant.Constant;
import com.pay.common.enums.NotifyStatus;
import com.pay.common.enums.OrderStatus;
import com.pay.common.enums.RoleType;
import com.pay.common.exception.Assert;
import com.pay.common.exception.CustomerException;
import com.pay.common.page.PageReqParams;
import com.pay.common.security.SecurityUtils;
import com.pay.common.utils.api.Base64Utils;
import com.pay.common.utils.api.Md5Utils;
import com.pay.common.utils.api.OrderParamKey;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.entity.SysUserEntity;
import com.pay.data.mapper.MerchantRepository;
import com.pay.data.mapper.OrderRepository;
import com.pay.data.mapper.SysUserRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.order.params.OrderAmountParams;
import com.pay.manager.pc.order.params.OrderPageReqParams;
import com.pay.manager.pc.order.params.OrderPageRespParams;
import com.pay.manager.pc.order.params.OrderQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl extends AbstractHelper<OrderRepository, OrderEntity, Long> implements OrderService {


    @Override
    public Page<OrderPageRespParams> selectOrderPage(OrderQuery orderQuery, PageReqParams reqParams) {
        RoleType roleType = SecurityUtils.getRoleType();
        if (RoleType.MERCHANT.equals(roleType)) {
            Long userId = SecurityUtils.getUserId();
            SysUserEntity user = sysUserRepository.getOne(userId);
            MerchantEntity merchant = user.getMerchant();
            Assert.mustBeTrue(!ObjectUtils.isEmpty(merchant), "商户角色数据有问题");
            orderQuery.setMerchantNo(merchant.getMerchantNo());
        }
        Page<OrderEntity> list = getPage(orderQuery, toPageable(reqParams, Constant.CREATE_TIME));
        return list.map(e -> pageCopy(e, OrderPageRespParams.class));
    }

    @Override
    public void sendCallback(OrderPageReqParams reqParams) {
        String merchantNo = reqParams.getMerchantNo();
        Map<String, String> map = new HashMap<>();
        map.put(OrderParamKey.merchNo.name(), reqParams.getMerchantNo());
        map.put(OrderParamKey.orderNo.name(), reqParams.getOrderNo());
        //map.put(OrderParamKey.businessNo.name(), reqParams.getBusinessNo());
        map.put(OrderParamKey.orderState.name(), String.valueOf(OrderStatus.succ.getCode()));
        BigDecimal realAmount = reqParams.getRealAmount();
        BigDecimal orderAmount = reqParams.getOrderAmount();
        map.put(OrderParamKey.amount.name(), StringUtils.isEmpty(realAmount) ? orderAmount.toString() : realAmount.toString());

        MerchantEntity merchant = merchantRepository.findByMerchantNo(merchantNo);
        String mPublicKey = merchant.getMd5Key();
        OrderEntity order = orderRepository.findByMerchant_MerchantNoAndOrderNo(merchantNo, reqParams.getOrderNo());

        byte[] context = JSON.toJSONBytes(map);
        String sign = Md5Utils.sign(new String(context, StandardCharsets.UTF_8), mPublicKey, "UTF-8");
        String type = "MD5";
        Map<String, String> params = new HashMap<>();
        params.put("sign", sign);
        params.put("context", Base64Utils.encode(context));
        params.put("encryptType", type);
        try {
            ResponseEntity<String> entity = restTemplate.postForEntity(order.getNotifyUrl(), params, String.class);
            Assert.mustBeTrue(Objects.equals(entity.getBody(), "ok"), "手动通知下游，下游返回：" + entity.getBody());
            log.info("订单:{},手动回调成功,更新订单状态为:{},回调状态为:{}", order.getOrderNo(), OrderStatus.succ, NotifyStatus.SUCCESS);
            order.setOrderStatus(OrderStatus.succ);
            order.setNotifyStatus(NotifyStatus.SUCCESS);
            this.save(order);
        } catch (Exception e) {
            throw new CustomerException("手动通知下游失败:" + e);
        }
    }

    @Override
    public List<OrderAmountParams> getOrderAmountByChannelFlag(int page) {

        List<OrderAmountParams> orderAmountParamsList = new ArrayList<>();
        System.out.println(SecurityUtils.getRoleType());
        if (RoleType.MERCHANT.equals(SecurityUtils.getRoleType())) {
            getList(new OrderQuery(getMerchantByLogin().getMerchantNo(), OrderStatus.succ))
                    .stream().collect(
                    Collectors.groupingBy(
                            OrderEntity::getChannel,
                            Collectors.summarizingDouble(e -> e.getOrderAmount().doubleValue())
                    )
            ).forEach((k, v) -> {
                double sum = v.getSum();
                OrderAmountParams orderAmountParams = new OrderAmountParams();
                orderAmountParams.setChannelFlag(k.getChannelName());
                orderAmountParams.setTotalAmount(new BigDecimal(sum));
                orderAmountParamsList.add(orderAmountParams);
            });
        } else {
            getList(new OrderQuery(OrderStatus.succ))
                    .stream().collect(
                    Collectors.groupingBy(
                            OrderEntity::getChannel,
                            Collectors.summarizingDouble(e -> e.getOrderAmount().doubleValue())
                    )
            ).forEach((k, v) -> {
                double sum = v.getSum();
                OrderAmountParams orderAmountParams = new OrderAmountParams();
                orderAmountParams.setChannelFlag(k.getChannelName());
                orderAmountParams.setTotalAmount(new BigDecimal(sum));
                orderAmountParamsList.add(orderAmountParams);
            });
        }
        orderAmountParamsList.sort(Comparator.comparing(OrderAmountParams::getTotalAmount).reversed());
        return orderAmountParamsList.stream().limit(page).collect(Collectors.toList());
    }

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MerchantRepository merchantRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    SysUserRepository sysUserRepository;
}
