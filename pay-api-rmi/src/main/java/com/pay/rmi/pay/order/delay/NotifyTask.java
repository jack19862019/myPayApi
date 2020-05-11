package com.pay.rmi.pay.order.delay;

import com.alibaba.fastjson.JSON;
import com.pay.common.enums.NotifyStatus;
import com.pay.common.enums.OrderStatus;
import com.pay.common.utils.api.Base64Utils;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.rmi.pay.constenum.OrderParamKey;
import com.pay.rmi.service.ApiMerchantService;
import com.pay.rmi.service.ApiOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class NotifyTask {

    private final Logger logger = LoggerFactory.getLogger(NotifyTask.class);

    //每次推迟时间基数 单位 秒
    private final long TIME_UNIT = 10;
    //每次通知失败后下次通知等待时间 与基数的倍数
    private final int TIME_TIMES = 3;
    //最大通知次数
    private final int TIMES_MAX = 6;
    //通知成功标识
    private final String RES_SUCCESS = "ok";

    //延迟队列
    private DelayQueue<DelayItem<OrderInfo>> queue = new DelayQueue<>();
    //守护线程
    private Thread taskThread;
    //通知线程池
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    //spring HTTP发送模板

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiOrderService orderService;

    @Autowired
    private ApiMerchantService merchantService;

    private NotifyTask() {
        taskThread = new Thread(() -> execute());
        taskThread.setName("NotifyTask Thread");
        taskThread.start();
    }

    private void execute() {
        while (true) {
            try {
                DelayItem<OrderInfo> di = queue.take();
                OrderInfo o = di.getItem();
                if (o != null) {
                    cachedThreadPool.execute(() -> {
                        int times = o.getTimes();
                        OrderEntity order = o.getOrder();
                        String notifyUrl = o.getNotifyUrl();
                        logger.info("订单号：{}，第{}次通知，内容：{}，通知地址：{}，原文内容：{}",
                                order.getOrderNo(),
                                times,
                                o.getParams(),
                                notifyUrl,
                                o.getOriginParams());
                        String result = null;
                        int statusCode = 404;
                        try {
                            ResponseEntity<String> entity = restTemplate.postForEntity(o.getNotifyUrl(), o.getParams(), String.class);
                            statusCode = entity.getStatusCodeValue();
                            result = entity.getBody();
                        } catch (RestClientException e) {
                            logger.error(e.getMessage(), e);
                        }
                        if (!RES_SUCCESS.equalsIgnoreCase(result)) {
                            logger.error("订单号：{},第{}次通知失败,HTTP状态码：{},返回内容：{}",
                                    order.getOrderNo(),
                                    times,
                                    statusCode,
                                    result);
                            if (times < TIMES_MAX) {
                                o.setTimes(o.getTimes() + 1);
                                put(o);
                            } else {
                                //超过6次通知失败，关闭订单
                                order.setNotifyStatus(NotifyStatus.FAILURE);
                                order.setOrderStatus(OrderStatus.close);
                                orderService.update(order);
                                logger.error("订单号：{},通知失败,NOTIFY END", order.getOrderNo());
                            }
                        } else {
                            order.setNotifyStatus(NotifyStatus.SUCCESS);
                            orderService.update(order);
                            logger.info("订单号：{},通知完成", order.getOrderNo());
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 加入通知队列
     *
     * @param o
     */
    public void put(OrderInfo o) {
        int times = o.getTimes() - 1 == 0 ? 0 : (int) Math.pow(TIME_TIMES, o.getTimes() - 1);
        long nanoTime = TIME_UNIT + TimeUnit.NANOSECONDS.convert(times * TIME_UNIT, TimeUnit.SECONDS);
        queue.put(new DelayItem<>(o, nanoTime));
    }

    @Value("${test.callback}")
    private String testCallBack;

    public void put(OrderEntity order) {
        String merchantNo = order.getMerchant().getMerchantNo();
        Map<String, String> map = new HashMap<>();
        map.put(OrderParamKey.merchNo.name(), order.getMerchant().getMerchantNo());
        map.put(OrderParamKey.orderNo.name(), order.getOrderNo());
        map.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
        map.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderStatus().getCode()));
        map.put(OrderParamKey.amount.name(), order.getRealAmount().toString());

        MerchantEntity merchant = merchantService.selectByMerchantNo(merchantNo);
        String mPublicKey = merchant.getMd5Key();

        byte[] context = JSON.toJSONBytes(map);
        String sign = Md5Utils.sign(new String(context, StandardCharsets.UTF_8), mPublicKey, "UTF-8");
        String type = "MD5";
        Map<String, String> params = new HashMap<>();
        params.put("sign", sign);
        params.put("context", Base64Utils.encode(context));
        params.put("encryptType", type);
        OrderInfo oInfo = new OrderInfo(order, params, JSON.toJSONString(map), "pro".equals(testCallBack) ? order.getNotifyUrl() : testCallBack);
        put(oInfo);
    }
}
