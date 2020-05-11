package com.pay.data.mapping;

import com.pay.data.mapping.params.ChannelRankParams;
import com.pay.data.mapping.params.ChannelTodayRespParams;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Mapper
@Repository
public interface HomePageMapper {
    //获取当天的通道金额与时间
    List<ChannelTodayRespParams> getChannelAmountBytoday(String channelFlag);

    //获取本周的通道金额与时间
    List<ChannelTodayRespParams> getChannelAmountByWeek(String channelFlag);

    //获取本月的通道金额与时间
    List<ChannelTodayRespParams> getChannelAmountByMonth(String channelFlag);

    //获取本年的通道金额与时间
    List<ChannelTodayRespParams> getChannelAmountByYear(String channelFlag);

    //点击查询某天
    List<ChannelTodayRespParams> getChannelDayAmountByClick(String day, String channelFlag);

    //点击查询某月
    List<ChannelTodayRespParams> getChannelMonthAmountByClick(String month, String channelFlag);
    
    //根据某天查询当天的通道总金额
    BigDecimal getChannelAmountByDate(String oneDate, String channelFlag);

    //获取按金额由大到小排列的通道的list
    List<ChannelRankParams> getChannelRank();
}
