package com.pay.manager.pc.statistics;


import com.pay.common.enums.DateIntervalType;
import com.pay.common.enums.DateMonthType;
import com.pay.common.enums.MerChaType;
import com.pay.common.enums.OrderStatus;
import com.pay.common.exception.Assert;
import com.pay.common.exception.CustomerException;
import com.pay.common.utils.DateUtil;
import com.pay.data.entity.OrderEntity;
import com.pay.data.mapper.OrderRepository;
import com.pay.data.supper.AbstractHelper;
import com.pay.manager.pc.statistics.params.XDateRespParams;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.pay.common.utils.DateUtil.*;
import static java.util.stream.Collectors.toList;

@Component
public class StatisticsHelper extends AbstractHelper<OrderRepository, OrderEntity, Long> {

    private Date date;

    private DateMonthType monthType;

    private DateIntervalType dateIntervalType;

    private List<XDateRespParams> respParams;

    StatisticsHelper setDate() {
        this.date = new Date();
        return this;
    }

    StatisticsHelper setDate(String scope) {
        this.date = toDate(scope);
        if (scope.matches("^\\d{4}-\\d{1,2}$")) {
            setDateInterval(monthType);
        } else if (scope.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$")) {
            setDateInterval(DateIntervalType.TODAY);
        }
        return this;
    }

    private void setDateInterval(DateMonthType monthType) {
        this.monthType = monthType;
        this.dateIntervalType = DateIntervalType.MONTH;
    }

    StatisticsHelper setDateInterval(DateIntervalType dateInterval) {
        this.dateIntervalType = dateInterval;
        return this;
    }

    StatisticsHelper execute() {
        Assert.mustBeTrue(!ObjectUtils.isEmpty(date), "【日期date】不能为空");
        Assert.mustBeTrue(!ObjectUtils.isEmpty(dateIntervalType), "【X轴类型DateIntervalType】不能为空");
        switch (dateIntervalType) {
            case TODAY:
                return getXTodayStr();
            case WEEK:
                return getXWeekStr();
            case MONTH:
                return getXMonthStr();
            case YEAR:
                return getXYearStr();
        }
        throw new CustomerException("请选择时间DateIntervalType类型");
    }

    private StatisticsHelper getXYearStr() {
        List<XDateRespParams> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            XDateRespParams respParams = new XDateRespParams();
            respParams.setName(String.format("%s月份", numberToZh(i)));
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            Date date = toDate(String.format(year + "-" + "%02d", i));
            respParams.setDateScope(toStr(DATE_MOUNTH, date));
            list.add(respParams);
        }
        this.respParams = list;
        return this;
    }


    private StatisticsHelper getXMonthStr() {
        int currentMonthDay = getDaysOfMonth(date);
        List<XDateRespParams> list = new ArrayList<>();
        for (int i = 1; i <= currentMonthDay; i++) {
            XDateRespParams respParams = new XDateRespParams();
            respParams.setName(String.format("%s月%s号", DateUtil.getYearMonthIndex(date), i));
            String futureDate = getFutureDate(getMonthStartTime(date), i - 1);
            respParams.setDateScope(futureDate);
            list.add(respParams);
        }
        this.respParams = list;
        return this;
    }


    private StatisticsHelper getXWeekStr() {
        Date startWeek = getWeekStartTime(date);
        List<XDateRespParams> list = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            XDateRespParams respParams = new XDateRespParams();
            respParams.setName(String.format("星期%s", DateUtil.numberToZh(i)));
            String futureDate = getFutureDate(startWeek, i - 1);
            respParams.setDateScope(futureDate);
            list.add(respParams);
        }
        this.respParams = list;
        return this;
    }

    private StatisticsHelper getXTodayStr() {
        String futureDate = toStr(DATE_PATTERN, date) + " %02d";
        List<XDateRespParams> list = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            XDateRespParams respParams = new XDateRespParams();
            respParams.setName(String.format("%s点", i));
            respParams.setDateScope(String.format(futureDate, i));
            list.add(respParams);
        }
        this.respParams = list;
        return this;
    }

    <T> List<T> sequence(List<CompletableFuture<T>> futures) {
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            return allFutures.thenApply(v -> futures.parallelStream().map(CompletableFuture::join).collect(toList())).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new CustomerException("异步任务CompletableFuture失败" + e);
        }
    }


    BigDecimal countAmount(String mocFlag, String scope, MerChaType merChaType) {
        switch (merChaType) {
            case CHANNEL:
                return repository.channelOrder(mocFlag, OrderStatus.succ.getCode(), "^" + scope);
            case MERCHANT:
                return repository.merchantOrder(mocFlag, OrderStatus.succ.getCode(), "^" + scope);
        }
        throw new CustomerException("查询统计异常");
    }


    protected List<XDateRespParams> get() {
        return respParams;
    }
}
