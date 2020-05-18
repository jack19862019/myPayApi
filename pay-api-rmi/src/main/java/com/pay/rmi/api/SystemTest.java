package com.pay.rmi.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import com.pay.common.utils.api.Md5Utils;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.MerchantEntity;
import com.pay.common.utils.api.Base64Utils;
import com.pay.data.entity.PayTypeEntity;
import com.pay.rmi.api.resp.*;
import com.pay.rmi.paythird.PayService;
import com.pay.rmi.paythird.PayServiceFactory;
import com.pay.rmi.paythird.baihuizhifu.BaiHui;
import com.pay.rmi.paythird.momozhifu.Momo;
import com.pay.rmi.service.ApiChannelService;
import com.pay.rmi.service.ApiMerchantChannelService;
import com.pay.rmi.service.ApiMerchantService;
import com.pay.rmi.service.ApiOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.parameters.P;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.toList;


@Slf4j
@RestController
public class SystemTest {

    @RequestMapping("/order/list")
    public Page<OrderListResp> md5encryption(String orderNo, String pageSize, String pageNum) {
        if (StringUtils.isEmpty(pageNum)){
            pageNum = "1";
        }
        if (StringUtils.isEmpty(pageSize)){
            pageSize = "10";
        }
        return orderService.selectByFlagAndOrderNo(orderNo, pageSize, pageNum);
    }

    /**
     * 模拟回调下游，接收自己发的通知
     */
    @RequestMapping("/merchant/encryption/content")
    public String md5encryption(@RequestBody EncryReqParams encryParams) {
        Map<String, String> map = new HashMap<>();
        byte[] context = JSON.toJSONString(encryParams.getParams()).getBytes(StandardCharsets.UTF_8);
        String sign = Md5Utils.sign(
                new String(context, StandardCharsets.UTF_8),
                encryParams.getMerchantKey(),
                "UTF-8"
        );
        map.put("sign", sign);
        map.put("context", Base64Utils.encode(context));
        map.put("encryptType", "MD5");
        return JSON.toJSONString(map);
    }


    @RequestMapping("/merchant/decryption/content")
    public String md5decryption(@RequestBody DecrypReqParams decrypReqParams) throws Exception {
        JSONObject jObj = JSON.parseObject(JSON.toJSONString(decrypReqParams));
        byte[] contexts = jObj.getBytes("context");
        JSONObject jsonObject = JSON.parseObject(new String(contexts, "UTF-8"));
        System.out.println("解密结果：" + jsonObject.toJSONString());
        String payForm = jsonObject.getString("pay_form");
        String qrcodeUrl = jsonObject.getString("qrcode_url");
        String codeUrl = jsonObject.getString("code_url");//code_url

        Map<String, String> map = new HashMap<>();
        if (!StringUtils.isEmpty(payForm)) {
            map.put("formData", payForm);
        }
        if (!StringUtils.isEmpty(qrcodeUrl)) {
            map.put("pictureData", qrcodeUrl);
        }
        if (!StringUtils.isEmpty(codeUrl)) {
            map.put("urlData", codeUrl);
        }
        return JSON.toJSONString(map);
    }


    //查询商户list（熊猫，365，K8）
    @RequestMapping("/merchant/list")
    public List<MerchantResp> getMerchant() {
        List<MerchantResp> list = new ArrayList<>();
        List<MerchantEntity> merchantEntities = merchantService.selectAllMerchant();
        for (MerchantEntity entity : merchantEntities) {
            MerchantResp resp = new MerchantResp();
            resp.setId(entity.getId());
            resp.setName(entity.getMerchantName());
            resp.setNum(entity.getMerchantNo());
            resp.setPublicKey(entity.getMd5Key());
            list.add(resp);
        }
        return list;
    }

    //查询商户下面的配置的通道（365->放心付，通汇宝，AA支付等）
    @RequestMapping("/{merchantNo}/channel")
    public List<ChannelResp> getMerchantChannel(@PathVariable String merchantNo) {
        return merchantChannelService.selectByMerchantNo(merchantNo);
    }

    //查询通道下面的支付方式
    @RequestMapping("/{channelNo}/payType")
    public List<PayTypeResp> getPayTypeByChannelNo(@PathVariable String channelNo){
        ChannelEntity channelEntity = channelService.selectByChannelNo(channelNo);

        return null;
    }


    public static void main(String[] args) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        Class<? extends PayService> aClass = Momo.class;
        PayService newInstance = aClass.newInstance();
        Field payTypeMap = newInstance.getClass().getDeclaredField("payTypeMap");
        payTypeMap.setAccessible(true);
        Object object = payTypeMap.get(newInstance);
        String jsonString = JSON.toJSONString(object);

        Map<String, String> map = JSON.parseObject(jsonString, new TypeReference<TreeMap<String, String>>() {
        });
        map.forEach((k, v) -> {
            System.out.println(k + "=" + SystemTestPayType.getName(k));
        });
    }

    @Autowired
    ApiOrderService orderService;

    @Autowired
    ApiChannelService channelService;

    @Autowired
    private ApiMerchantService merchantService;

    @Autowired
    private ApiMerchantChannelService merchantChannelService;
}
