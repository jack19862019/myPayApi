package com.pay.manager.pc.cache;

import com.pay.common.utils.SequenceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class CacheService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String numCreate(String str) {
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Long num = this.getIncrementNum(str + "Number" + currentDate);
        String matchNum = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        return str + matchNum + SequenceUtils.getSequence(num);
    }

    public Long getIncrementNum(String key) {
        // 不存在准备创建 键值对
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        long counter = entityIdCounter.incrementAndGet();
        if (counter == 1) {// 初始设置过期时间
            entityIdCounter.expire(1, TimeUnit.DAYS);//单位天
        }
        return counter;
    }
}