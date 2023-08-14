package com.xqy.gulimall.coupon.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.xqy.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.xqy.gulimall.coupon.service.SeckillSkuRelationService;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSkuRelationEntity> relationEntityQueryWrapper = new QueryWrapper<>();
        String promotionSessionId = (String) params.get("promotionSessionId");
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            relationEntityQueryWrapper.eq("sku_id", key).or().eq("promotion_session_id", key);
        }
        if (!StringUtils.isEmpty(promotionSessionId)) {
            relationEntityQueryWrapper.eq("promotion_session_id", promotionSessionId);
        }
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                relationEntityQueryWrapper
        );

        return new PageUtils(page);
    }

}