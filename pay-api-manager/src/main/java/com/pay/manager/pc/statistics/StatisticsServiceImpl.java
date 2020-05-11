package com.pay.manager.pc.statistics;

import com.pay.common.enums.DateIntervalType;
import com.pay.common.enums.MerChaType;
import com.pay.common.enums.OrderStatus;
import com.pay.common.enums.RoleType;
import com.pay.common.exception.Assert;
import com.pay.common.security.SecurityUtils;
import com.pay.data.entity.OrderEntity;
import com.pay.data.mapper.OrderRepository;
import com.pay.manager.pc.order.params.OrderQuery;
import com.pay.manager.pc.statistics.params.DataRespParams;
import com.pay.manager.pc.statistics.params.XDateRespParams;
import com.pay.manager.pc.statistics.params.YDataRespParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Service
public class StatisticsServiceImpl extends StatisticsHelper implements StatisticsService {

    @Autowired
    OrderRepository orderRepository;

    @Override
    public Long getCountOrder() {
        return RoleType.MERCHANT.equals(SecurityUtils.getRoleType()) ?
                count(new OrderQuery(getMerchantByLogin().getMerchantNo())) : count();
    }

    @Override
    public BigDecimal getCountOrderAmount() {
        return RoleType.MERCHANT.equals(SecurityUtils.getRoleType())?
                getList(new OrderQuery(getMerchantByLogin().getMerchantNo(), OrderStatus.succ))
                        .parallelStream().map(OrderEntity::getOrderAmount).reduce(BigDecimal.ZERO, BigDecimal::add):
                getList(new OrderQuery(OrderStatus.succ))
                        .parallelStream().map(OrderEntity::getOrderAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    @Override
    public List<DataRespParams> getIndexHistogram(String mocFlag, DateIntervalType dateIntervalType, String dateScope, MerChaType merChaType) {
        long start1 = System.currentTimeMillis();
        List<XDateRespParams> list = StringUtils.isEmpty(dateScope)
                ? setDate().setDateInterval(dateIntervalType).execute().get()
                : setDate(dateScope).execute().get();
        Assert.mustBeTrue(!CollectionUtils.isEmpty(list), "X轴数据为空");
        long end1 = System.currentTimeMillis();
        System.out.println("查询X轴数据总时间:"+(end1-start1));

        long start = System.currentTimeMillis();
        List<CompletableFuture<BigDecimal>> allFutures = list.parallelStream().map(XDateRespParams::getDateScope)
                .map(e -> CompletableFuture.supplyAsync(() -> countAmount(mocFlag, e, merChaType))).collect(toList());
        long end = System.currentTimeMillis();
        System.out.println("查询【"+allFutures.size()+"】次sql总时间:"+(end-start));

        long start2 = System.currentTimeMillis();
        List<BigDecimal> amountList = sequence(allFutures);
        long end2 = System.currentTimeMillis();
        System.out.println("异步集成Y轴数据总时间:"+(end2-start2));

        long start3 = System.currentTimeMillis();
        List<DataRespParams> collect = IntStream.range(0, list.size()).mapToObj(i -> new DataRespParams(list.get(i), new YDataRespParams(amountList.get(i)))).collect(toList());
        long end3 = System.currentTimeMillis();
        System.out.println("组装数据花费时间:"+(end3-start3));
        return collect;
    }
}

