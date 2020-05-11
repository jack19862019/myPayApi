package com.pay.rmi.pay.constenum;


/**
 * @ClassName: OutChannel
 * @Description: 支付通道枚举类 微信WAP支付：wap 微信公众号支付：gzh 微信扫码：wx QQ钱包扫码：qq
 * 支付宝扫码：ali 快捷支付：q 网银支付：wy 代付：acp
 * @date 2017年10月24日 下午8:02:11
 */
public enum OutChannel {
    //微信支付
    wechatpay,
    //微信H5
    wechath5,
    //微信wap，
    wechatwap,
    //微信公众号
    wechatgz,
    //微信D1
    wechatpayd1,
    //微信h5D1
    wechath5d1,
    //微信wapD1
    wechatwapd1,
    //微信小程序
    wechatxcx,
    //微信原生扫码
    wechatyssm,
    //微信原生h5
    wechatysh5,
    //微信话费
    wechatHF,
    //支付宝
    alipay,
    //支付宝原生扫码
    aliyssm,
    //支付宝h5
    alih5,
    //支付宝wap
    aliwap,
    //支付宝pdd
    alipdd,
    //支付宝红包
    aliredbox,
    //支付宝红包
    alism,
    //支付宝红包扫码
    aliredboxsm,
    //支付宝原生h5
    aliysh5,
    //支付宝扫码d1
    alipayd1,
    //支付宝h5D1
    alih5d1,
    //支付宝wapD1
    aliwapd1,
    //支付宝转账
    alipaytrans,
    //支付宝转银行卡
    alipay2bank,
    //支付宝个码H5
    alipaygemah5,
    //支付宝转卡H5
    alipayzkh5,
    //支付宝话费
    alipayhuafei,
    //微信话费
    wechathuafei,
    //qq
    qqpay,
    //qqh5
    qqh5,
    //qqwap
    qqwap,
    //京东
    jdpay,
    //京东扫码
    jdpaysm,
    //京东h5
    jdh5,
    //京东wap
    jdwap,
    //百度
    baidupay,
    //百度h5
    baiduh5,
    //百度wap
    baiduwap,
    //云闪付
    unionpay,
    //云闪付h5
    unionh5,
    //云闪付wap
    unionwap,
    //快捷支付
    quickpay,
    //网上银行
    //中信
    bank_citic,
    //邮政储蓄
    bank_psbc,
    //浦东发展
    bank_spdb,
    //兴业
    bank_cib,
    //华夏
    bank_hxb,
    //民生
    bank_cmbc,
    //光大
    bank_ceb,
    //招商
    bank_cmb,
    //交通
    bank_bcom,
    //建设
    bank_ccb,
    //农业
    bank_abc,
    //中国,
    bank_boc,
    //工商
    bank_icbc,
    //银联扫码
    unionsm,
    //银联wap
    unionbankwap,
    //银联快捷
    unionquickpay,
    //银联网关
    unionwg,
    //快捷H5
    quickpayh5,
    //网银
    onlinepay,
    //网银扫码
    onlinepaysm,
    //代付
    acp,
    //玩家自选网关支付
    wjzx,
    //财付通
    cft,
    //PDD
    pdd,
    //话费
    hf,
    //大额卡转卡/微信/支付宝
    zk;
}
