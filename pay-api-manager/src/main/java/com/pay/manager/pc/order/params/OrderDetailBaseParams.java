package com.pay.manager.pc.order.params;

import com.pay.common.enums.EncryptionType;
import com.pay.common.enums.NotifyStatus;
import com.pay.common.enums.OrderStatus;
import com.pay.common.enums.OrderType;
import com.pay.data.entity.ChannelEntity;
import com.pay.data.entity.MerchantEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDetailBaseParams {

    private String orderNo;

    private String businessNo;

    private String merchantNo;

    private String merchantName;

    private String channelFlag;

    private String channelName;

    private String upPayTypeName;

    private String upPayTypeFlag;

    private String payTypeName;

    private String payTypeFlag;

    private String upMerchantNo;

    private String upKey;

    private EncryptionType encryptionType;

    private BigDecimal orderAmount;

    private BigDecimal realAmount;

    private OrderStatus orderStatus;

    private NotifyStatus notifyStatus;

    private OrderType orderType;

    private String returnUrl;

    private String notifyUrl;

    private String userId;

    private String reqIp;

    private List<OrderLogParams> orderLogParamsList;


}
