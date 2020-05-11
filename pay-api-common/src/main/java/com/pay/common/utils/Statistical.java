package com.pay.common.utils;

import java.util.Calendar;

public class Statistical {
    /**
     * 根据年 月 获取对应的月份 天数
     * */
    public static int getDaysByYearMonth(int year, int month) {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }
    /**
     * 让数据好看点
     * */
    public static String fill(int i) {
        if(i<10){
            return "0"+i;
        }else {
            return i+"";
        }
    }
}
