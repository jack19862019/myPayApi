package com.pay.rmi.api;

import com.alibaba.fastjson.JSON;
import com.pay.common.exception.Assert;
import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.data.entity.OrderEntity;
import com.pay.data.params.OrderReqParams;
import com.pay.rmi.api.req.OrderApiParams;
import com.pay.rmi.api.resp.OrderApiRespParams;
import com.pay.rmi.common.exception.RException;
import com.pay.rmi.common.utils.DateUtil;
import com.pay.rmi.common.utils.HttpRequestUtils;
import com.pay.rmi.common.utils.MapToXml;
import com.pay.rmi.common.utils.R;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.PayServiceFactory;
import com.pay.rmi.service.ApiChannelService;
import com.pay.rmi.service.ApiMerchantChannelService;
import com.pay.rmi.service.ApiMerchantService;
import com.pay.rmi.service.ApiOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

@RestController
@Slf4j
public class Api {

    @PostMapping("order")
    public R order(@RequestBody OrderApiParams apiParams) {
        long start = System.currentTimeMillis();
        String str = DateUtil.parseTimeSecStrSSS(start);
        CheckResult checkResult = checkRequestHelper.commDataCheck(apiParams);
        //获取下游上传参数
        MerchantEntity merchant = checkResult.getMerchant();
        ChannelEntity channel = checkResult.getChannel();
        OrderReqParams reqParams = checkResult.getReqParams();
        McpConfigEntity mcpConfig = checkResult.getMcpConfigEntity();

        String orderNo = reqParams.getOrderNo();//订单号
        String merchantNo = reqParams.getMerchNo();//商户号
        String channelNo = reqParams.getChannelNo();//通道编号
        log.info("====================订单开始时间：{},订单号：{}", str, orderNo);
        //根据下游发起的支付请求中带的通道标识 获取对应的上游支付类
        PayService payService = payServiceFactory.getService(channel.getChannelFlag());

        //校验订单重复
        OrderEntity orderEntity = orderService.selectByMerchNoAndOrderNo(merchantNo, orderNo);
        Assert.mustBeTrue(orderEntity == null, "支付订单重复,重复单号:" + orderNo);
        //随机ip
        doBusinessIp(reqParams);

        long startThird = System.currentTimeMillis();
        String startThirdStr = DateUtil.parseTimeSecStrSSS(startThird);
        log.info("--------------------调用支付开始时间：{}, 通道：{},订单号：{}", startThirdStr, channelNo, orderNo);

        OrderApiRespParams respParams;
        try {
            respParams = payService.order(channel, mcpConfig, reqParams);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            throw new RException(channel.getChannelName(), e.getMessage(), e);
        }
        long endThird = System.currentTimeMillis();
        String endStrThird = DateUtil.parseTimeSecStrSSS(endThird);
        log.info("--------------------调用支付结束时间：{}, 通道：{},订单号：{}", endStrThird, channelNo, orderNo);
        log.info("--------------------调用支付累计耗时：{}, 通道：{},订单号：{}", (endThird - startThird) + "毫s", channelNo, orderNo);

        R res = decryptAndSign(respParams, merchant.getMd5Key());
        long end = System.currentTimeMillis();
        String enStr = DateUtil.parseTimeSecStrSSS(end);
        log.info("====================订单结束时间：{},订单号：{}", enStr, orderNo);
        log.info("====================订单累计耗时：{},订单号：{}", (end - start) + "毫s", orderNo);
        return res;
    }

    private void doBusinessIp(OrderReqParams reqParams) {
        String reqIp = reqParams.getReqIp();
        if (StringUtils.isEmpty(reqIp) || "127.0.0.1".equals(reqIp)) {
            //随机给一个ip
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("117.191.11.");
            //随机两位数
            Random rand = new Random();
            String matchNum = String.valueOf(rand.nextInt(90) + 10);
            stringBuilder.append(matchNum);
            reqParams.setReqIp(stringBuilder.toString());
        }
    }

    @RequestMapping("localhost/test/call/back")
    public String acceptCallBack() {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>异步回调成功");
        return "异步回调成功!";
    }

    @RequestMapping("callback/{channelNo}/{merchantNo}/{orderNo}")
    public String callback(@PathVariable String channelNo, @PathVariable String merchantNo, @PathVariable String orderNo, HttpServletRequest request) throws UnsupportedEncodingException {
        long startThird = System.currentTimeMillis();
        String startThirdStr = DateUtil.parseTimeSecStrSSS(startThird);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>回调信息开始{},请求头:", startThirdStr, request.getContentType());
        Map<String, String> params;
        if (!ObjectUtils.isEmpty(request.getContentType()) && request.getContentType().contains(MediaType.APPLICATION_XML_VALUE)) {
            String xmlContent = MapToXml.getRequestBody(request);
            log.info(">>>>>>>>>>>>>>>>>>>>>>>回调信息开始:" + xmlContent);
            params = MapToXml.toMap(xmlContent);
        } else {
            params = HttpRequestUtils.commonHttpRequestParamConvert(request);
        }
        log.info(">>>>>>>>>>>>>>>>>>>>>>>回调信息开始 channelNo: {}, merchantNo: {}, orderNo: {}, params: {}",
                channelNo, merchantNo, orderNo, JSON.toJSONString(params));
        Assert.mustBeTrue(!CollectionUtils.isEmpty(params), "回调参数异常,为空");
        OrderEntity order = orderService.selectByMerchNoAndOrderNo(merchantNo, orderNo);
        if (ObjectUtils.isEmpty(order)) {
            return "您回调的订单不存在,由于支付请求时，响应状态失败或支付请求超时未处理。您可以忽略此订单";
        }
        ChannelEntity channel = channelService.selectByChannelNo(channelNo);
        //MerchantEntity merchant = merchantService.selectByMerchantNo(merchantNo);
        McpConfigEntity mcpConfig = merchantChannelService.selectByMerchantNoAndChannelNo(merchantNo, channelNo);
        PayService payService = payServiceFactory.getService(channel.getChannelFlag());
        return payService.callback(order, mcpConfig, params);
    }


    private R decryptAndSign(OrderApiRespParams respParams, String mPublicKey) {
        try {
            log.info("返回明文数据:" + JSON.toJSONString(respParams));
            byte[] context = JSON.toJSONBytes(respParams);
            String sign = Md5Utils.sign(new String(context, StandardCharsets.UTF_8), mPublicKey, "UTF-8");
            return R.ok().put("sign", sign).put("context", context);
        } catch (Exception e) {
            log.error("返回数据 公钥加密，私钥签名 失败！");
        }
        return R.error("返回数据 公钥加密，私钥签名 失败！");
    }


    @Autowired
    private PayServiceFactory payServiceFactory;

    @Autowired
    private ApiMerchantService merchantService;

    @Autowired
    private ApiChannelService channelService;

    @Autowired
    private ApiMerchantChannelService merchantChannelService;

    @Autowired
    private ApiOrderService orderService;

    @Autowired
    private CheckRequestHelper checkRequestHelper;
}
