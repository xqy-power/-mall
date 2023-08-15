package com.xqy.gulimall.coupon.service.impl;

import com.xqy.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.xqy.gulimall.coupon.service.SeckillSkuRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.coupon.dao.SeckillSessionDao;
import com.xqy.gulimall.coupon.entity.SeckillSessionEntity;
import com.xqy.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSessionEntity> sessionEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            sessionEntityQueryWrapper.eq("id", key).or().eq("name", key);
        }
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                sessionEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 得到lates3天会议
     *
     * @return {@link List}<{@link SeckillSessionEntity}>
     */
    @Override
    public List<SeckillSessionEntity> getLates3DaysSession() {
        //计算最近三天
        List<SeckillSessionEntity> sessionEntities = this.baseMapper.selectList(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        if (sessionEntities != null && sessionEntities.size() > 0){
            List<SeckillSessionEntity> collect = sessionEntities.stream().map(item -> {
                List<SeckillSkuRelationEntity> promotionSessionId = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", item.getId()));
                item.setRelationSkus(promotionSessionId);
                return item;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    private String startTime() {
        LocalTime min = LocalTime.MIN; //00:00:00
        LocalDate now = LocalDate.now(); //2023-08-15
        return LocalDateTime.of(now, min).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));  //2023-08-15 00:00:00
    }

    private String endTime() {
        LocalTime max = LocalTime.MAX; //23:59:59
        LocalDate plus = LocalDate.now().plusDays(2); //2023-08-17
        return LocalDateTime.of(plus, max).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); //2023-08-17 23:59:59
    }


}