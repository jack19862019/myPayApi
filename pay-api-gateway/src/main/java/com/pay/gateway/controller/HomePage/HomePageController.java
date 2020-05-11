package com.pay.gateway.controller.HomePage;

import com.pay.common.enums.DateIntervalType;
import com.pay.common.utils.Result;
import com.pay.data.leaderboard.*;
import com.pay.data.mapping.StatisticalDao;
import com.pay.data.statistical.StatisticService;
import com.pay.data.statistical.StatisticalQuery;
import com.pay.gateway.async.AsyncSetter;
import com.pay.gateway.async.AsyncSetterFactory;
import com.pay.manager.pc.homePage.HomePageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "首页-展示接口")
@RequestMapping("/sys/homepage")
public class HomePageController {
    @Autowired
    private HomePageService homePageService;
    @Autowired
    StatisticService statisticaService;
    @Autowired
    private StatisticalDao statisticalDao;
    @GetMapping("/channel")
    @ApiOperation(value = "通道金额展示")
    public Result getChannelAmount(DateIntervalType dateType, Long channelId, String day) throws ParseException {
        Map<String, String> map;
        if ("".equals(day) || day==null){
            map = homePageService.getChannelAmount(dateType,channelId);
        }else {
            map = homePageService.getChannelAmountByClick(day,channelId);
        }

        return Result.success(map);
    }

    @GetMapping("/getChannelRank")
    @ApiOperation(value = "通道排名展示")
    public Result getChannelRank() throws ParseException {

        return Result.success(homePageService.getChannelRank());
    }

    @GetMapping("/merchants")
    @ApiOperation(value = "商户金额统计")
    public Result getLogs(StatisticalQuery statisticalQuery) {
        return Result.success(statisticaService.selectByMerchantNo(statisticalQuery));
    }

    @GetMapping("/merchantsRanking")
    @ApiOperation(value = "商户统计排行")
    public Result merchantsRanking() {
        return Result.success(statisticaService.merchantsRanking());
    }

    @Autowired
    AsyncSetterFactory asyncSetterFactory;
    @GetMapping("/larboard")
    @ApiOperation(value = "排行榜")
    public Result getLarboard(OrderQuery orderQuery) {
        AsyncSetter<OrderParams> asyncSetter = asyncSetterFactory.getAsyncSetter();
        asyncSetter.setOriginal(new OrderParams())
                .addRunAble(orderParams -> countChannel(orderParams,orderQuery))
                .addRunAble(orderParams -> countMerchant(orderParams,orderQuery))
                .addRunAble(orderParams -> countOrder(orderParams,orderQuery))
                .addRunAble(orderParams -> countOrderAmount(orderParams,orderQuery))
                .addRunAble(orderParams -> orderAmountParams(orderParams,orderQuery))
                .addRunAble(orderParams -> orderTypeAmountParams(orderParams,orderQuery))
                .execute();
        return Result.success(asyncSetter.getOriginal());
    }


    public void countChannel(OrderParams orderParams,OrderQuery orderQuery){
        if(orderQuery.getMerchantNo()!=null){
            orderParams.setCountChannel(statisticalDao.findByMerchantNoCountChannel(orderQuery.getMerchantNo()));
        }else{
            orderParams.setCountChannel(statisticalDao.findCountChannel());
        }
    }
    public void countMerchant(OrderParams orderParams,OrderQuery orderQuery){
        if(orderQuery.getMerchantNo()!=null){
            orderParams.setCountMerchant(statisticalDao.findByMerchantNoTopUpDay(orderQuery.getMerchantNo()));
        }else{
            orderParams.setCountMerchant(statisticalDao.findByMerchantNoCountMerchant());
        }
    }
    public void countOrder(OrderParams orderParams,OrderQuery orderQuery){
        orderParams.setCountOrder(statisticalDao.findByMerchantNoCountOrder(orderQuery.getMerchantNo()));
    }
    public void countOrderAmount(OrderParams orderParams,OrderQuery orderQuery){
        orderParams.setCountOrderAmount(statisticalDao.findByMerchantNoCountOrderAmount(orderQuery.getMerchantNo()));
    }
    public void orderAmountParams(OrderParams orderParams,OrderQuery orderQuery){
        List<OrderAmountParams> orderAmountParams = statisticalDao.findByMerchantNoOrderAmountByChannelFlag(orderQuery.getMerchantNo(),orderQuery.getPage());
        orderParams.setOrderAmountParams(orderAmountParams);
    }
    public void orderTypeAmountParams(OrderParams orderParams,OrderQuery orderQuery){
        List<OrderTypeAmountParams> orderTypeAmountParams = statisticalDao.findByMerchantNoOrderAmountByPayTypeFlag(orderQuery.getMerchantNo());
        OrderTypeAmountParams orderTypeAmount = statisticalDao.findByMerchantNoOther(orderQuery.getMerchantNo());
        orderTypeAmountParams.add(orderTypeAmount);
        orderParams.setOrderTypeAmountParams(orderTypeAmountParams);
    }
}
