package com.pay.manager.pc.order.params;

import com.pay.common.annotation.Query;
import com.pay.common.enums.MerChaType;
import com.pay.common.enums.OrderStatus;
import com.pay.data.querybase.BaseQueryCriteria;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderQuery extends BaseQueryCriteria {

    @Query(type = Query.Type.INNER_LIKE)
    private String orderNo;

    //商户名称
    @Query(type = Query.Type.INNER_LIKE, propName = "merchant.merchantName")
    private String merchantName;

    @ApiModelProperty(hidden = true)
    @Query(type = Query.Type.EQUAL, propName = "merchant.merchantNo")
    private String merchantNo;

    @ApiModelProperty(hidden = true)
    @Query(type = Query.Type.EQUAL, propName = "merchant.id")
    private Long merchantId;

    @ApiModelProperty(hidden = true)
    @Query(type = Query.Type.EQUAL, propName = "channel.id")
    private Long channelId;

    @ApiModelProperty(hidden = true)
    @Query(type = Query.Type.EQUAL, propName = "channel.channelFlag")
    private String channelFlag;

    //支付平台名称（通道名称）
    @Query(type = Query.Type.INNER_LIKE, propName = "channel.channelName")
    private String channelName;

    //支付方式名称
    @Query(type = Query.Type.EQUAL, propName = "payType.payTypeFlag")
    private String payTypeFlag;

    //订单状态（枚举）
    @Query(type = Query.Type.EQUAL)
    private OrderStatus orderStatus;

    //请求时间
    @Query(type = Query.Type.BETWEEN)
    private String createTime;

    public OrderQuery(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public OrderQuery(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public OrderQuery(String merchantNo, OrderStatus orderStatus) {
        this.merchantNo = merchantNo;
        this.orderStatus = orderStatus;
    }
}
