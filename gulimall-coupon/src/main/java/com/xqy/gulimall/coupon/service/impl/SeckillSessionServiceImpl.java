package com.xqy.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
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

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSessionEntity> sessionEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (key != null) {
            sessionEntityQueryWrapper.eq("id", key).or().eq("name", key);
        }
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                sessionEntityQueryWrapper
        );

        return new PageUtils(page);
    }

}