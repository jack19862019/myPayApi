package com.pay.manager.pc.homePage;

import com.pay.common.enums.DateIntervalType;
import com.pay.data.mapping.params.ChannelRankParams;
import com.pay.data.mapping.params.ChannelTodayRespParams;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface HomePageService {
    //当天，本周，本月，本年
    Map<String, String> getChannelAmount(DateIntervalType dateType, Long channelId) throws ParseException;

    //点击查询某天，某月
    Map<String, String>  getChannelAmountByClick(String day, Long channelId) throws ParseException;

    //获取按金额由大到小排列的通道的list
    List<ChannelRankParams> getChannelRank();
}
