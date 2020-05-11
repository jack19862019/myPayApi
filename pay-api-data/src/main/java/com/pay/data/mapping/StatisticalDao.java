package com.pay.data.mapping;

import com.pay.data.entity.StatisticalEntiey;
import com.pay.data.leaderboard.OrderAmountParams;
import com.pay.data.leaderboard.OrderMerchant;
import com.pay.data.leaderboard.OrderTypeAmountParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Mapper
@Repository
public interface StatisticalDao {
    //获取当天的盘口金额与时间
    @Select("<script> SELECT HOUR (create_time) `hour`, SUM(real_amount) count FROM pay_order WHERE date_format(create_time, '%Y-%m-%d') = #{queryTime} AND order_status=1  <if test=\"merchantNo !=null and merchantNo !=''\"> AND merchant_no = #{merchantNo} </if>  GROUP BY date_format(create_time, '%Y%m%d-%H'), HOUR </script>")
    List<StatisticalEntiey> findByMerchantNoDay(String merchantNo,String queryTime);

    //获取当周的盘口金额与时间
    @Select("<script> SELECT date_format(create_time, '%Y-%m-%d') as `hour`,SUM(real_amount) as count FROM pay_order WHERE YEARWEEK( date_format( create_time,'%Y-%m-%d' ),1 ) = YEARWEEK( now( ) ,1) AND order_status=1  <if test=\"merchantNo !=null and merchantNo !=''\"> AND merchant_no = #{merchantNo} </if> GROUP BY DAY(create_time) , HOUR </script>")
    List<StatisticalEntiey> findByMerchantNoWeeks(String merchantNo,String queryTime);

    //获取当月的盘口金额与时间
    @Select("<script> SELECT day as `hour`, max(amount) as count FROM ( SELECT DAY(ord.create_time) AS day,IF (sum(ord.real_amount) IS NULL, 0, sum(ord.real_amount)) AS amount\n" +
            " FROM pay_order AS ord WHERE MONTH(date_format(ord.create_time,'%Y-%m-%d')) = MONTH(#{queryTime}) AND order_status=1  <if test=\"merchantNo !=null and merchantNo !=''\"> AND merchant_no = #{merchantNo} </if> GROUP BY DAY(ord.create_time)\n" +
            " UNION SELECT * from ( select 1, 0 UNION select 2, 0 UNION select 3, 0 UNION select 4, 0 UNION select 5, 0 UNION select 6, 0 UNION select 7, 0 UNION select 8, 0 UNION select 9, 0 UNION\n" +
            " select 10, 0\n" +
            " UNION\n" +
            " select 11, 0\n" +
            " UNION\n" +
            " select 12, 0\n" +
            " UNION\n" +
            " select 13, 0\n" +
            " UNION\n" +
            " select 14, 0\n" +
            " UNION\n" +
            " select 15, 0\n" +
            " UNION\n" +
            " select 16, 0\n" +
            " UNION\n" +
            " select 17, 0\n" +
            " UNION\n" +
            " select 18, 0\n" +
            " UNION\n" +
            " select 19, 0\n" +
            " UNION\n" +
            " select 20, 0\n" +
            " UNION\n" +
            " select 21, 0\n" +
            " UNION\n" +
            " select 22, 0\n" +
            " UNION\n" +
            " select 23, 0\n" +
            " UNION\n" +
            " select 24, 0\n" +
            " UNION\n" +
            " select 25, 0\n" +
            " UNION\n" +
            " select 26, 0\n" +
            " UNION\n" +
            " select 27, 0\n" +
            " UNION\n" +
            " select 28, 0\n" +
            " UNION\n" +
            " select 29, 0\n" +
            " UNION\n" +
            " select 30, 0\n" +
            " UNION\n" +
            " select 31, 0\n" +
            " ) b\n" +
            " ) c\n" +
            "        group by day; </script>")
    List<StatisticalEntiey> findByMerchantNoMonth(String merchantNo,String queryTime);

    //获取当年的盘口金额与时间
    @Select("<script> SELECT SUM(real_amount) AS count,MONTH(create_time) as hour FROM pay_order WHERE order_status=1  <if test=\"merchantNo !=null and merchantNo !=''\"> AND merchant_no = #{merchantNo} </if> GROUP BY MONTH(create_time) </script>")
    List<StatisticalEntiey> findByMerchantNoYears(String merchantNo,String queryTime);


    //今日充值
    @Select("<script> SELECT IFNULL(SUM(real_amount),0) as countOrderAmount from pay_order WHERE order_status=1 AND TO_DAYS(create_time) = TO_DAYS(NOW()) <if test=\"@org.apache.commons.lang3.StringUtils@isNotEmpty(merchantNo)\"> AND merchant_no = #{merchantNo} </if> </script>")
    long findByMerchantNoTopUpDay(@Param("merchantNo") String merchantNo);
    //查询入住商户
    @Select("<script> SELECT IFNULL(count(id),0) as countMerchant from merchant WHERE is_delete = 1</script>")
    long findByMerchantNoCountMerchant();
    //总支付通道
    @Select("<script> SELECT IFNULL(count(distinct id),0) from channel WHERE is_delete = 1</script>")
    long findCountChannel();
    //查询商户支付通道
    @Select("<script> SELECT IFNULL(count(distinct c.channel_id),0) from mcp_config c LEFT JOIN merchant m ON m.id=c.merchant_id WHERE c.is_delete = 1 <if test=\"@org.apache.commons.lang3.StringUtils@isNotEmpty(merchantNo)\"> AND m.merchant_no = #{merchantNo} </if></script>")
    long findByMerchantNoCountChannel(@Param("merchantNo") String merchantNo);
    //查询商户订单
    @Select("<script> SELECT IFNULL(count(id),0) as countOrder from pay_order WHERE order_status=1 <if test=\"@org.apache.commons.lang3.StringUtils@isNotEmpty(merchantNo)\"> AND merchant_no = #{merchantNo} </if></script>")
    long findByMerchantNoCountOrder(@Param("merchantNo") String merchantNo);
    //查询充值金额
    @Select("<script> SELECT IFNULL(SUM(real_amount),0) as countOrderAmount from pay_order WHERE order_status=1 <if test=\"@org.apache.commons.lang3.StringUtils@isNotEmpty(merchantNo)\"> AND merchant_no = #{merchantNo} </if></script>")
    BigDecimal findByMerchantNoCountOrderAmount(@Param("merchantNo") String merchantNo);
    //查询通道排行榜
    @Select("<script> SELECT IFNULL(SUM(po.real_amount),0) as totalAmount,(SELECT c.channel_name from channel as c where c.channel_flag = po.channel_flag) AS channelFlag from pay_order as po WHERE po.order_status=1 <if test=\"@org.apache.commons.lang3.StringUtils@isNotEmpty(merchantNo)\"> AND po.merchant_no = #{merchantNo} </if> GROUP BY po.channel_flag ORDER BY totalAmount DESC LIMIT 0,#{page} </script>")
    List<OrderAmountParams> findByMerchantNoOrderAmountByChannelFlag(@Param("merchantNo") String merchantNo,int page);
    //查询支付方式排行榜
    @Select("<script> SELECT IFNULL(SUM(po.real_amount),0) as totalAmount,(SELECT pay_type_name FROM pay_type p WHERE p.pay_type_flag = po.pay_type_flag AND p.is_delete = 1) AS payTypeFlag from pay_order po WHERE po.order_status=1 <if test=\"@org.apache.commons.lang3.StringUtils@isNotEmpty(merchantNo)\"> AND po.merchant_no = #{merchantNo} </if> GROUP BY po.pay_type_flag ORDER BY totalAmount DESC LIMIT 0,4 </script>")
    List<OrderTypeAmountParams> findByMerchantNoOrderAmountByPayTypeFlag(@Param("merchantNo") String merchantNo);
    //查询其他金额
    @Select("<script> SELECT ((SELECT IFNULL(SUM(real_amount), 0) FROM pay_order WHERE order_status = 1 <if test=\"@org.apache.commons.lang3.StringUtils@isNotEmpty(merchantNo)\"> AND merchant_no = #{merchantNo} </if>)-(SELECT SUM(a.total) total from (SELECT IFNULL(SUM(real_amount), 0) as total FROM pay_order WHERE order_status = 1 <if test=\"@org.apache.commons.lang3.StringUtils@isNotEmpty(merchantNo)\"> AND merchant_no = #{merchantNo} </if> GROUP BY pay_type_flag ORDER BY total DESC LIMIT 0,4) as a)) AS totalAmount,\"其他\" AS payTypeFlag FROM merchant LIMIT 0,1 </script>")
    OrderTypeAmountParams findByMerchantNoOther(@Param("merchantNo") String merchantNo);

    //商户排行
    @Select("<script> SELECT SUM(pay_order.real_amount) as amount,merchant_no AS merchantNo,(SELECT merchant_name FROM merchant WHERE merchant.merchant_no=pay_order.merchant_no) AS merchantName FROM pay_order\n" +
            "GROUP BY merchant_no\n" +
            "ORDER BY amount DESC </script>")
    List<OrderMerchant> merchantsRanking();

}
