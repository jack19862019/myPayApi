package com.pay.data.statistical;

import com.pay.common.enums.DateIntervalType;
import com.pay.common.exception.CustomerException;
import com.pay.common.utils.Statistical;
import com.pay.data.entity.StatisticalEntiey;
import com.pay.data.leaderboard.OrderMerchant;
import com.pay.data.mapping.StatisticalDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class StatisticServiceImpl implements StatisticService {

    @Autowired
    private StatisticalDao statisticalDao;


    @Override
    public Map<String, String> selectByMerchantNo(StatisticalQuery statisticalQuery) {
        DateIntervalType type=statisticalQuery.getDateType();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        if(StringUtils.isNotBlank(statisticalQuery.getDay())){
            if(statisticalQuery.getDay().length()<=7){
                type=DateIntervalType.getStatusByCode(2);
            }else{
                type=DateIntervalType.getStatusByCode(0);
            }
        }
        switch (type){
            case TODAY:
                if(!StringUtils.isNotBlank(statisticalQuery.getDay())){
                    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
                    statisticalQuery.setDay(timeFormat.format(new Date()));
                }
                return dayFormat(statisticalDao.findByMerchantNoDay(statisticalQuery.getMerchantNo(),statisticalQuery.getDay()));
            case WEEK:
                return weeksFormat(statisticalDao.findByMerchantNoWeeks(statisticalQuery.getMerchantNo(),statisticalQuery.getDay()));
            case MONTH:
                if(!StringUtils.isNotBlank(statisticalQuery.getDay())){
                    statisticalQuery.setDay(sdf.format(new Date()));
                }
                return monthFormat(statisticalDao.findByMerchantNoMonth(statisticalQuery.getMerchantNo(),statisticalQuery.getDay()+"-01"),statisticalQuery.getDay());
            case YEAR:
                if(!StringUtils.isNotBlank(statisticalQuery.getDay())){
                    statisticalQuery.setDay(sdf.format(new Date()));
                }
                return yearsFormat(statisticalDao.findByMerchantNoYears(statisticalQuery.getMerchantNo(),statisticalQuery.getDay()),statisticalQuery.getDay());
        }
        throw new CustomerException("首页统计传入时间类型不对", 500);
    }

    @Override
    public  List<OrderMerchant> merchantsRanking() {
        return statisticalDao.merchantsRanking();
    }

    private  Map<String, String> dayFormat(List<StatisticalEntiey> staList){
        Map<String, String> map=new LinkedHashMap<>();
        //让数据变得好看点
        int i=0;
        Integer hour;
        for (StatisticalEntiey sta:staList) {
            hour = Integer.parseInt(sta.getHour());
            for (;i<=hour;i++){
                if(hour==i){
                    map.put(Statistical.fill(i)+":00",sta.getCount());
                }else {
                    map.put(Statistical.fill(i)+":00","0.00");
                }
            }
        }
        if(i<=23){
            for (;i<=23;i++){
                map.put(Statistical.fill(i)+":00","0.00");
            }
        }
        return map;
    }

    private  Map<String, String> weeksFormat(List<StatisticalEntiey> staList){
        Map<String, String> map=new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c=Calendar.getInstance(Locale.CHINA);
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        map.put(sdf.format(c.getTime()),"0.00");
        for (int i=1;i<=6;i++) {
            c.add(Calendar.DAY_OF_MONTH, 1);
            map.put(sdf.format(c.getTime()),"0.00");
        }
        for (StatisticalEntiey sta:staList) {
            map.put(sta.getHour(),sta.getCount());
        }
        return map;
    }

    private  Map<String, String> monthFormat(List<StatisticalEntiey> staList,String day){
        Integer years=Integer.parseInt(day.substring(0,4));
        Integer month=Integer.parseInt(day.substring(5));
        Map<String, String> map=new LinkedHashMap<>();
        int maxDaysByDate = Statistical.getDaysByYearMonth(years, month);
        for (int i=1;i<=maxDaysByDate;i++) {
            map.put(years+"-"+Statistical.fill(month)+"-"+Statistical.fill(i),staList.get(i-1).getCount());
        }
        return map;
    }
    private  Map<String, String> yearsFormat(List<StatisticalEntiey> staList,String day){
        Integer years=Integer.parseInt(day.substring(0,4));
        Map<String, String> map=new LinkedHashMap<>();
        //让数据变得好看点
        int i=1;
        Integer hour;
        for (StatisticalEntiey sta:staList) {
            hour = Integer.parseInt(sta.getHour());
            for (;i<=hour;i++){
                if(hour==i){
                    map.put(years+"-"+Statistical.fill(i),sta.getCount());
                }else {
                    map.put(years+"-"+Statistical.fill(i),"0.00");
                }
            }
        }
        if(i<12){
            for (;i<=12;i++){
                map.put(years+"-"+Statistical.fill(i),"0.00");
            }
        }
        return map;
    }

}
