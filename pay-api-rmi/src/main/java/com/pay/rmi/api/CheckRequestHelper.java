package com.pay.rmi.api;

import com.alibaba.fastjson.JSON;
import com.pay.common.exception.Assert;
import com.pay.common.utils.api.Md5Utils;
import com.pay.common.validator.ValidationUtils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.McpConfigEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.rmi.api.req.OrderApiParams;
import com.pay.rmi.api.req.OrderReqParams;
import com.pay.rmi.common.utils.MatchUtils;
import com.pay.rmi.service.ApiChannelService;
import com.pay.rmi.service.ApiMerchantChannelService;
import com.pay.rmi.service.ApiMerchantService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class CheckRequestHelper {

    private Logger logger = LoggerFactory.getLogger(CheckRequestHelper.class);

    private CheckResult checkResult = new CheckResult();

    private String merchantNo;

    private String channelNo;


    CheckResult commDataCheck(OrderApiParams apiParams) {
        String sign = apiParams.getSign();
        logger.info("请求签名:{}", sign);
        byte[] context = apiParams.getContext();
        //解密
        String source = new String(context, StandardCharsets.UTF_8);
        logger.info("解密结果:{}", source);
        OrderReqParams orderReqParams = JSON.parseObject(source, OrderReqParams.class);
        ValidationUtils.validate(orderReqParams);
        //校验商户与商户通道信息
        String merchantNo = orderReqParams.getMerchNo();
        String channelNo = orderReqParams.getChannelNo();
        checkMerchant(merchantNo).checkChannel(channelNo).checkMerchantChannel();

        MerchantEntity merchant = checkResult.getMerchant();
        logger.info("参数解密密钥:{}", merchant.getMd5Key());
        Assert.mustBeTrue((Md5Utils.verify(source, sign, merchant.getMd5Key(), "UTF-8")), "下游请求验签失败");

        //补全一些基本的数据信息，第三方可能一定要有值的
        orderReqParams.setProduct(StringUtils.isEmpty(orderReqParams.getProduct()) ? getProduct(orderReqParams) : orderReqParams.getProduct());
        checkResult.setReqParams(orderReqParams);
        return this.checkResult;
    }

    private String getProduct(OrderReqParams reqParams) {
        return reqParams.getMerchNo() + MatchUtils.generateShortUuid();
    }

    private void checkMerchantChannel() {
        McpConfigEntity mcpConfigEntity = merchantChannelService.selectByMerchantNoAndChannelNo(merchantNo, channelNo);
        Assert.mustBeTrue(mcpConfigEntity != null, "上游通道信息不存在,商户号:" + merchantNo + ",通道标识:" + channelNo);
        String upMerchantNo = mcpConfigEntity.getUpMerchantNo();
        Assert.mustBeTrue(StringUtils.isNotEmpty(upMerchantNo), "上游通道商户号不存在,MCP id为" + mcpConfigEntity.getId());
        checkResult.setMcpConfigEntity(mcpConfigEntity);
    }


    private CheckRequestHelper checkChannel(String channelNo) {
        ChannelEntity channel = channelService.selectByChannelNo(channelNo);
        Assert.mustBeTrue(ObjectUtils.isNotEmpty(channel), "您传入的通道:" + channelNo + ",不存在或该通道已被禁用");
        checkResult.setChannel(channel);
        this.channelNo = channelNo;
        return this;
    }

    private CheckRequestHelper checkMerchant(String merchantNo) {
        MerchantEntity merchant = merchantService.selectByMerchantNo(merchantNo);
        Assert.mustBeTrue(ObjectUtils.isNotEmpty(merchant), "商户不存在" + merchantNo);
        checkResult.setMerchant(merchant);
        this.merchantNo = merchantNo;
        return this;
    }

    @Autowired
    private ApiMerchantService merchantService;

    @Autowired
    private ApiChannelService channelService;

    @Autowired
    private ApiMerchantChannelService merchantChannelService;

}
