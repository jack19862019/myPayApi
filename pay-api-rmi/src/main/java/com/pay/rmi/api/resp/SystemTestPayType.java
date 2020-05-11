package com.pay.rmi.api.resp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum SystemTestPayType implements Serializable {
    wechatpay("wechatpay", "微信支付"),
    wechath5("wechath5", "微信H5"),
    wechatwap("wechatwap", "微信wap"),
    wechatgz("wechatgz", "微信公众号"),
    wechatpayd1("wechatpayd1", "微信D1"),
    wechath5d1("wechath5d1", "微信h5D1"),
    wechatwapd1("wechatwapd1", "微信wapD1"),
    alipay("alipay", "支付宝"),
    alih5("alih5", "支付宝h5"),
    aliwap("aliwap", "支付宝wap"),
    aliredbox("aliredbox", "支付宝红包"),
    aliredboxsm("aliredboxsm", "支付宝红包扫码"),
    alism("alism", "支付宝扫码"),
    alipayd1("alipayd1", "支付宝扫码d1"),
    alih5d1("alih5d1", "支付宝h5D1"),
    aliwapd1("aliwapd1", "支付宝wapD1"),
    qqpay("qqpay", "QQ扫码"),
    qqh5("qqh5", "QQH5"),
    qqwap("qqwap", "QQWAP"),
    jdpay("jdpay", "京东"),
    jdh5("jdh5", "京东h5"),
    jdwap("jdwap", "京东wap"),
    baidupay("baidupay", "百度"),
    baiduwap("baiduwap", "百度wap"),
    zfbpt("zfbpt", "支付宝pt"),
    zfbsmpt("zfbsmpt", "支付宝扫码pt"),
    wechatxcx("wechatxcx", "微信小程序"),
    wechatyssm("wechatyssm", "微信原生扫码支付"),
    wechatysh5("wechatysh5", "微信原生h5支付"),
    aliyssm("aliyssm", "支付宝原生扫码支付"),
    aliysh5("aliysh5", "支付宝原生h5支付"),
    jdpaysm("jdpaysm", "京东扫码"),
    onlinepaysm("onlinepaysm", "网银扫码"),
    unionpay("unionpay", "云闪付"),
    unionh5("unionh5", "云闪付h5"),
    unionwap("unionwap", "云闪付wap"),
    quickpay("quickpay", "快捷支付"),
    bank_citic("bank_citic", "中信"),
    bank_psbc("bank_psbc", "邮政储蓄"),
    bank_spdb("bank_spdb", "浦东发展"),
    bank_cib("bank_cib", "兴业"),
    bank_hxb("bank_hxb", "华夏"),
    bank_cmbc("bank_cmbc", "民生"),
    bank_ceb("bank_ceb", "光大"),
    bank_cmb("bank_cmb", "招商"),
    bank_bcom("bank_bcom", "交通"),
    bank_ccb("bank_ccb", "建设"),
    bank_abc("bank_abc", "农业"),
    bank_boc("bank_boc", "中国"),
    bank_icbc("bank_icbc", "工商"),
    unionsm("unionsm", "银联扫码"),
    unionquickpay("unionquickpay", "银联快捷"),
    quickpayh5("quickpayh5", "快捷H5"),
    onlinepay("onlinepay", "网银"),
    acp("acp", "代付"),
    wjzx("wjzx", "玩家自选网关支付"),
    cft("cft", "财付通"),
    zk("zk", "大额卡转卡/微信/支付宝"),
    yyzfbzk("yyzfbzk", "支付宝转卡"),
    yyzfbgmh5("yyzfbgmh5", "支付宝个码H5"),
    yytbzf("yytbzf", "淘宝支付宝H5"),
    yywysgm("yywysgm", "伪原生个码"),
    yyjhzf("yyjhzf", "聚合支付");


    private String code;
    private String name;

    SystemTestPayType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static Map<String, SystemTestPayType> map = new HashMap<>();

    private static Map<String, SystemTestPayType> mapString = new HashMap<>();


    public static SystemTestPayType getStatusByName(String name) {
        if (mapString == null || mapString.isEmpty()) {
            mapString = new HashMap<>();
            for (SystemTestPayType status : SystemTestPayType.values()) {
                mapString.put(status.getName(), status);
            }
        }
        return map.get(name);
    }

    public static SystemTestPayType getStatusByCode(String code) {
        if (map == null || map.isEmpty()) {
            map = new HashMap<>();
            for (SystemTestPayType status : SystemTestPayType.values()) {
                map.put(status.getCode(), status);
            }
        }
        return map.get(code);
    }


    public static String getName(String code) {
        SystemTestPayType type = getStatusByCode(code);
        return type == null ? "" : type.getName();
    }

    public String getCode(SystemTestPayType sex) {
        return sex.getCode();
    }


}
