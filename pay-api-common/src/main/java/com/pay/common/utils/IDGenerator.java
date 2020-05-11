package com.pay.common.utils;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.Date;

/**
 * Configurable 自定义id生产策略
 * 雪花算法
 */
public class IDGenerator implements IdentifierGenerator {

    //角色编号生成器
    public static String roleNumCreate() {
        StringBuilder buffer = new StringBuilder("JS");
        String format = DateUtil.toStr(DateUtil.TIME_PATTERN, new Date());
        return buffer.append(format).toString();
    }


    public static void setProcessId(long processId) {
        IDGenerator.processId = processId;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        return getId();
    }

    private final static long beginTs = 1483200000000L;

    private static long lastTs = 0L;

    private static long processId;

    private static long sequence = 0L;

    private static synchronized Serializable getId() {
        long ts = timeGen();
        if (ts < lastTs) {// 刚刚生成的时间戳比上次的时间戳还小，出错
            throw new RuntimeException("时间戳顺序错误");
        }
        int sequenceBits = 6;
        if (ts == lastTs) {// 刚刚生成的时间戳跟上次的时间戳一样，则需要生成一个sequence序列号
            // sequence循环自增
            sequence = (sequence + 1) & ((1 << sequenceBits) - 1);
            // 如果sequence=0则需要重新生成时间戳
            if (sequence == 0) {
                // 且必须保证时间戳序列往后
                ts = nextTs(lastTs);
            }
        } else {// 如果ts>lastTs，时间戳序列已经不同了，此时可以不必生成sequence了，直接取0
            sequence = 0L;
        }
        lastTs = ts;// 更新lastTs时间戳
        int processIdBits = 6;
        return ((ts - beginTs) << (processIdBits + sequenceBits)) | (processId << sequenceBits)
                | sequence;
    }


    private static long timeGen() {
        return System.currentTimeMillis();
    }


    private static long nextTs(long lastTs) {
        long ts = timeGen();
        while (ts <= lastTs) {
            ts = timeGen();
        }
        return ts;
    }


}
