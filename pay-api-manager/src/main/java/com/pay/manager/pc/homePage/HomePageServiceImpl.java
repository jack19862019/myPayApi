package com.pay.manager.pc.homePage;

import com.pay.common.enums.DateIntervalType;
import com.pay.common.utils.DateUtil;
import com.pay.data.mapping.HomePageMapper;
import com.pay.data.mapping.params.ChannelRankParams;
import com.pay.data.mapping.params.ChannelTodayRespParams;
import com.pay.manager.pc.channel.ChannelService;
import com.pay.manager.pc.channel.params.ChannelDetailRespParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class HomePageServiceImpl implements HomePageService {
    @Autowired
    private ChannelService channelService;
    @Autowired
    private HomePageMapper homePageMapper;

    @Override
    public Map<String, String> getChannelAmount(DateIntervalType dateType, Long channelId) throws ParseException {
        ChannelDetailRespParams channelDetailRespParams = channelService.select(channelId);
        String channelFlag = channelDetailRespParams.getChannelFlag();
        List<ChannelTodayRespParams> list;
        Map<String, String> map = new LinkedHashMap<>();
        switch (dateType.getName()) {
            case "日":
                list = homePageMapper.getChannelAmountBytoday(channelFlag);
                for (ChannelTodayRespParams params : list
                ) {
                    String str;
                    if (params.getHour()< 10){
                        str = "0" + params.getHour().toString() + ":00";
                    }else {
                        str = params.getHour().toString() + ":00";
                    }
                    map.put(str, params.getAmount().toString());
                }
                break;
            case "周":
                SimpleDateFormat former = new SimpleDateFormat("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance();
                Date firstWeekDay = DateUtil.getThisWeekMonday(new Date());//获取本周第一天
                calendar.setTime(firstWeekDay);
                for (int i = 0; i < 7; i++) {
                    String firstWeekDayStr = former.format(calendar.getTime());
                    BigDecimal channelAmountByDate = homePageMapper.getChannelAmountByDate(firstWeekDayStr, channelFlag);
                    String amount = channelAmountByDate==null?"0.00":channelAmountByDate.toString();
                    map.put(firstWeekDayStr,amount);
                    calendar.add(Calendar.DATE, 1);
                }
                break;
            case "月":
                list = homePageMapper.getChannelAmountByMonth(channelFlag);
                Calendar curStartCal = Calendar.getInstance();
                Calendar cal = (Calendar) curStartCal.clone();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
                System.out.println(cal.getTime());
                for (ChannelTodayRespParams params : list
                ) {
                    params.setDateDisplay(DateUtil.toStr("yyyy-MM-dd", cal.getTime()));
                    cal.add(Calendar.DATE, 1);
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                String month = format.format(new Date(System.currentTimeMillis()));
                int monthTh = Integer.parseInt(month.substring(month.length()-2,month.length()));//第几个月份
                switch (monthTh){
                    case 2:
                        list.remove(30);
                        list.remove(29);
                        list.remove(28);
                        break;
                    case 4:
                    case 6:
                    case 8:
                    case 10:
                    case 12:
                        list.remove(30);
                        break;
                }

                for (ChannelTodayRespParams params : list
                ) {
                    map.put(params.getDateDisplay(), params.getAmount().toString());
                }
                break;
            case "年":
                list = homePageMapper.getChannelAmountByYear(channelFlag);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
                SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM");
                Date date = new Date(System.currentTimeMillis());
                String thisYear = formatter.format(date);
                //今年第一个月
                String firstMonthStr = thisYear + "-01";
                Date firstMonth = formatter2.parse(firstMonthStr);
                Calendar calen = Calendar.getInstance();
                calen.setTime(firstMonth);
                for (ChannelTodayRespParams params : list
                ) {
                    params.setDateDisplay(DateUtil.toStr("yyyy-MM", calen.getTime()));
                    calen.set(Calendar.MONTH, calen.get(Calendar.MONTH) + 1);
                }
                for (ChannelTodayRespParams params : list
                ) {
                    map.put(params.getDateDisplay(), params.getAmount().toString());
                }
                break;
            default:
                throw new IllegalStateException("参数错误" + dateType.getName());
        }
        return map;
    }

    @Override
    public Map<String, String> getChannelAmountByClick(String day, Long channelId) throws ParseException {
        ChannelDetailRespParams channelDetailRespParams = channelService.select(channelId);
        String channelFlag = channelDetailRespParams.getChannelFlag();
        List<ChannelTodayRespParams> list;
        Map<String, String> map = new LinkedHashMap<>();
        if (day.length() <= 7) {//点击某个月份展示天
            String month = day;
            String monthStr = month + "-01";//必须给月份加一个天数，不然sql查询结果有问题
            list = homePageMapper.getChannelMonthAmountByClick(monthStr, channelFlag);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Date date = sdf.parse(month);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            for (ChannelTodayRespParams params : list
            ) {
                params.setDateDisplay(DateUtil.toStr("yyyy-MM-dd", calendar.getTime()));
                calendar.add(Calendar.DATE, 1);
            }
            int monthTh = Integer.parseInt(month.substring(month.length()-2,month.length()));//第几个月份
            switch (monthTh){
                case 2:
                    list.remove(30);
                    list.remove(29);
                    list.remove(28);
                    break;
                case 4:
                case 6:
                case 8:
                case 10:
                case 12:
                    list.remove(30);
                    break;
            }
            for (ChannelTodayRespParams params : list
            ) {
                map.put(params.getDateDisplay(), params.getAmount().toString());
            }
        } else {//点击某一天展示小时
            list = homePageMapper.getChannelDayAmountByClick(day, channelFlag);
            for (ChannelTodayRespParams params : list
            ) {
                String str;
                if (params.getHour()< 10){
                    str = "0" + params.getHour().toString() + ":00";
                }else {
                    str = params.getHour().toString() + ":00";
                }
                map.put(str, params.getAmount().toString());
            }
        }
        return map;

    }

    @Override
    public List<ChannelRankParams> getChannelRank() {

        return homePageMapper.getChannelRank();
    }
}
