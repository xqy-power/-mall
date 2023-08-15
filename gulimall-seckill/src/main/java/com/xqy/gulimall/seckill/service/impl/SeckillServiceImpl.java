package com.xqy.gulimall.seckill.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.xqy.common.utils.R;
import com.xqy.gulimall.seckill.feign.CouponFeignService;
import com.xqy.gulimall.seckill.feign.ProductFeignService;
import com.xqy.gulimall.seckill.service.SeckillService;
import com.xqy.gulimall.seckill.to.SeckillRedisTo;
import com.xqy.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.xqy.gulimall.seckill.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 秒杀服务impl
 *
 * @author xqy
 * @date 2023/08/15
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    public static final String SECKILL_SESSIONS = "seckill:sessions:"; //活动【信息】
    public static final String SECKILL_SKUS = "seckill:skus:"; //商品【信息】
    public static final String SECKILL = "seckill:stock:";  //商品【库存】 随机码
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;



    /**
     * 上传秒杀sku latest3天
     */
    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1.扫秒最近三天需要参与秒杀的活动
        R r = couponFeignService.getLates3DaysSession();
        if (r.getCode() == 0) {
            //上架活动
            List<SeckillSessionsWithSkus> sessionsData = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //缓存到Redis
            //1.缓存活动信息
            saveSessionInfos(sessionsData);
            //2.缓存活动关联的商品信息
            saveSessionSkuInfos(sessionsData);
        }
    }

    /**
     * 得到当前秒杀sku
     *
     * @return {@link List}<{@link SeckillRedisTo}>
     */
    @Override
    public List<SeckillRedisTo> getCurrentSeckillSkus() {
        //1.确定当前时间属于哪个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SECKILL_SESSIONS + "*");
        if (keys != null) {
            for (String key : keys) {
                String replace = key.replace(SECKILL_SESSIONS, "");
                String[] split = replace.split("_");
                long start =Long.parseLong(split[0]);
                long end =Long.parseLong(split[1]);
                if (time >= start && time <= end) {
                    //2.获取这个秒杀场次的所有商品信息
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_SKUS);
                    if (range!=null) {
                        List<String> list = hashOps.multiGet(range);
                        if (list != null) {
                            List<SeckillRedisTo> collect = list.stream().map(item -> {
                                SeckillRedisTo redisTo = JSON.parseObject(item, SeckillRedisTo.class);
//                                redisTo.setRandomCode(null);   当前秒杀开始就需要返回随机码  不开始不需要返回随机码
                                return redisTo;
                            }).collect(Collectors.toList());
                            return collect;
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }

    /**
     * 保存会话信息
     *
     * @param sessionsData 会话数据
     */
    private void  saveSessionInfos(List<SeckillSessionsWithSkus> sessionsData) {
        sessionsData.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SECKILL_SESSIONS + startTime + "_" + endTime;
            Boolean b = redisTemplate.hasKey(key);
            if (Boolean.FALSE.equals(b)) {
                List<String> collect = session.getRelationSkus().stream().map(item->{
                    return item.getPromotionSessionId().toString()+"-"+item.getSkuId().toString();
                }).collect(Collectors.toList());
                //缓存活动信息
                redisTemplate.opsForList().leftPushAll(key, collect);
            }

        });
    }

    /**
     * 保存会话sku信息
     *
     * @param sessionsData 会话数据
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessionsData) {
        sessionsData.forEach(session->{
            //准备hash操作
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SECKILL_SKUS);

            session.getRelationSkus().forEach(seckillVo -> {
                Boolean key = hashOps.hasKey(seckillVo.getPromotionSessionId().toString() + "-" + seckillVo.getSkuId().toString());
                if (Boolean.FALSE.equals(key)){

                    SeckillRedisTo seckillRedisTo = new SeckillRedisTo();
                    //1.sku基本数据
                    R r = productFeignService.getSkuInfo(seckillVo.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo s = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        seckillRedisTo.setSkuInfo(s);
                    }

                    //2.sku的秒杀信息
                    BeanUtils.copyProperties(seckillVo, seckillRedisTo);

                    //3.设置当前商品的秒杀时间信息
                    seckillRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillRedisTo.setEndTime(session.getEndTime().getTime());

                    //4.设置随机码
                    String randomCode = StringUtils.replace(UUID.randomUUID().toString(), "-", "");
                    seckillRedisTo.setRandomCode(randomCode);

                    //5.设置商品的秒杀信号量 使用库存作为信号量 限流
                    redissonClient.getSemaphore(SECKILL + randomCode).trySetPermits(seckillVo.getSeckillCount());

                    //6.缓存商品信息
                    String string = JSON.toJSONString(seckillRedisTo);
                    hashOps.put(seckillVo.getPromotionSessionId().toString()+"-"+seckillVo.getSkuId().toString(), string);
                }
            });

        });
    }
}
