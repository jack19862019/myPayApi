package com.pay.rmi.service;


import com.pay.data.entity.McpConfigEntity;
import com.pay.rmi.api.resp.ChannelResp;

import java.util.List;

public interface ApiMerchantChannelService {

    McpConfigEntity selectByMerchantNoAndChannelNo(String merchantNo, String channelNo);

    List<ChannelResp> selectByMerchantNo(String merchantNo);
}
