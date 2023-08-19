package com.xqy.gulimall.seckill.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xqy.common.to.SeckillOrderTo;
import com.xqy.common.utils.R;
import com.xqy.gulimall.seckill.feign.CouponFeignService;
import com.xqy.gulimall.seckill.feign.ProductFeignService;
import com.xqy.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.xqy.gulimall.seckill.service.SeckillService;
import com.xqy.gulimall.seckill.to.SeckillRedisTo;
import com.xqy.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.xqy.gulimall.seckill.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.event.ItemEvent;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.toolkit.IdWorker.*;

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
    public static final String MemberSecKill = "seckillMember:";
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;


    /**
     * 上传秒杀sku latest3天
     */
    //TODO 每个场次的商品的过期时间
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
            if (sessionsData != null) {
                saveSessionInfos(sessionsData);
                //2.缓存活动关联的商品信息
                saveSessionSkuInfos(sessionsData);
            }
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
                long start = Long.parseLong(split[0]);
                long end = Long.parseLong(split[1]);
                if (time >= start && time <= end) {
                    //2.获取这个秒杀场次的所有商品信息
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_SKUS);
                    if (range != null) {
                        List<String> list = hashOps.multiGet(range);
                        if (list != null) {
                            return list.stream().map(item -> {
                                //                                redisTo.setRandomCode(null);   当前秒杀开始就需要返回随机码  不开始不需要返回随机码
                                return JSON.parseObject(item, SeckillRedisTo.class);
                            }).collect(Collectors.toList());
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public SeckillRedisTo getSkfuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_SKUS);
        Set<String> keys = hashOps.keys();
        if (keys != null && !keys.isEmpty()) {
            String reg = "\\d-" + skuId;
            List<String> stringList = new ArrayList<String>();

            //获取所有的匹配的key值
            for (String key : keys) {
                if (Pattern.matches(reg, key)) {
                    stringList.add(key);
                }
            }

            //根据key值获取所有的value值
            List<String> redisToList = hashOps.multiGet(stringList);

            //所有的value值   转换为SeckillRedisTo对象  在根据SecKillRedisTo::getStartTime
            // 获取到最近的秒杀场次中的该商品信息   （因为每一个商品  可以上架在任意一个场次中  在商品详情页需要展示最近的场次中该商品信息）
            Optional<SeckillRedisTo> redisTo = redisToList.stream().map(item -> {
                return JSON.parseObject(item, SeckillRedisTo.class);
            }).filter(item -> {
                long current = new Date().getTime();
                return current >= item.getStartTime() && current <= item.getEndTime();
            }).min(Comparator.comparing(SeckillRedisTo::getStartTime));
            if (redisTo.isPresent()) {
                SeckillRedisTo seckillRedisTo = redisTo.get();
                //随机码问题 秒杀开始就需要返回随机码  不开始不需要返回随机码
                long current = new Date().getTime();
                if (!(current >= seckillRedisTo.getStartTime() && current <= seckillRedisTo.getEndTime())) {
                    seckillRedisTo.setRandomCode(null);
                }
                return seckillRedisTo;
            }

        }


        return null;
    }

    /**
     * 秒杀下订单
     *
     * @param killId 杀死id
     * @param key    关键
     * @param num    全国矿工工会
     * @return {@link String}
     */
    @Override
    @Transactional
    //TODO 订单后续的根据Redisson信号量统一扣除库存
    public String kill(String killId, String key, Integer num) throws InterruptedException {
        //1.获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_SKUS);
        String json = hashOps.get(killId);
        if (!StringUtils.isEmpty(json)) {
            SeckillRedisTo seckillRedisTo = JSON.parseObject(json, SeckillRedisTo.class);//判断是否为空
            //2.判断合法性
            Long startTime = seckillRedisTo.getStartTime();
            Long endTime = seckillRedisTo.getEndTime();
            long current = new Date().getTime();
            if (current >= startTime && current <= endTime) {
                //在秒杀时间内   合法
                //3.验证随机码和商品id
                String randomCode = seckillRedisTo.getRandomCode();
                String skuIdKey = seckillRedisTo.getPromotionSessionId().toString() + "-" + seckillRedisTo.getSkuId().toString();
                if (randomCode.equals(key) && skuIdKey.equals(killId)) {
                    //验证码和商品id都正确  合法
                    //4.验证购物数量
                    if (num <= seckillRedisTo.getSeckillLimit()) {
                        //合法
                        //5.验证这个人是否已经购买过了 保证幂等性  一个人只能买一次 如果秒杀成功了 就去占位  userId-sessionId-skuId
                        String redisKey = LoginUserInterceptor.loginUser.get().getId().toString()
                                + "-" + seckillRedisTo.getPromotionSessionId().toString()
                                + "-" + seckillRedisTo.getSkuId().toString();
                        Boolean b = redisTemplate.opsForValue().setIfAbsent(MemberSecKill + redisKey, num.toString(), endTime - current, TimeUnit.MILLISECONDS);
                        if (Boolean.TRUE.equals(b)) {
                            //说明这个人没有购买过
                            //6.秒杀成功  快速下单  发送MQ消息
                            boolean tryAcquire = redissonClient.getSemaphore(SECKILL + randomCode).tryAcquire(num);
                            if (Boolean.TRUE.equals(tryAcquire)) {
                                //秒杀成功  快速下单  发送MQ消息
                                String orderSn = IdWorker.getTimeId();
                                SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                seckillOrderTo.setOrderSn(orderSn);
                                seckillOrderTo.setMemberId(LoginUserInterceptor.loginUser.get().getId());
                                seckillOrderTo.setNum(num);
                                seckillOrderTo.setPromotionSessionId(seckillRedisTo.getPromotionSessionId());
                                seckillOrderTo.setSkuId(seckillRedisTo.getSkuId());
                                seckillOrderTo.setSeckillPrice(seckillRedisTo.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTo);
                                return orderSn;
                            } else {
                                return null;
                            }

                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 保存秒杀场次信息
     *
     * @param sessionsData 会话数据
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessionsData) {
        sessionsData.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SECKILL_SESSIONS + startTime + "_" + endTime;
            Boolean b = redisTemplate.hasKey(key);
            if (Boolean.FALSE.equals(b)) {
                List<String> collect = session.getRelationSkus().stream().map(item -> {
                    return item.getPromotionSessionId().toString() + "-" + item.getSkuId().toString();
                }).collect(Collectors.toList());
                //缓存活动信息
                redisTemplate.opsForList().leftPushAll(key, collect);
            }

        });
    }

    /**
     * 保存秒杀sku信息
     *
     * @param sessionsData 会话数据
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessionsData) {
        sessionsData.forEach(session -> {
            //准备hash操作
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SECKILL_SKUS);

            session.getRelationSkus().forEach(seckillVo -> {
                Boolean key = hashOps.hasKey(seckillVo.getPromotionSessionId().toString() + "-" + seckillVo.getSkuId().toString());
                if (Boolean.FALSE.equals(key)) {

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
                    hashOps.put(seckillVo.getPromotionSessionId().toString() + "-" + seckillVo.getSkuId().toString(), string);
                }
            });

        });
    }
}
