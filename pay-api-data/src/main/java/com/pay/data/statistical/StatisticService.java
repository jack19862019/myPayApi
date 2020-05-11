package com.pay.data.statistical;


import com.pay.data.leaderboard.OrderMerchant;

import java.util.List;
import java.util.Map;

public interface StatisticService {

    Map<String,String> selectByMerchantNo(StatisticalQuery statisticalQuery);

    List<OrderMerchant> merchantsRanking();

}
