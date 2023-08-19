package com.xqy.gulimall.seckill.service;

import com.xqy.gulimall.seckill.to.SeckillRedisTo;

import java.util.List;

public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SeckillRedisTo> getCurrentSeckillSkus();

    SeckillRedisTo getSkfuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num) throws InterruptedException;
}
