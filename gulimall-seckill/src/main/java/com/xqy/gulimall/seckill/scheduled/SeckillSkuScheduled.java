package com.xqy.gulimall.seckill.scheduled;

import com.xqy.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀sku计划
 * 秒杀商品定时上架
 * 每天晚上三点，上架最近三天需要秒杀的商品
 * 当天00:00:00 - 23:59:59
 * 明天00:00:00 - 23:59:59
 * 后天00:00:00 - 23:59:59
 *
 * @author xqy
 * @date 2023/08/15
 */

@Service
@Slf4j
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock = "seckill:upload:lock";

    /**
     * 上传秒杀sku latest3天
     * 0 0 3 * * ?
     */
    //TODO 接口幂等性
    @Scheduled(cron = "*/10 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        //1.重复上架无需处理
        //分布式锁
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
        log.info("上架秒杀商品");
    }
}
