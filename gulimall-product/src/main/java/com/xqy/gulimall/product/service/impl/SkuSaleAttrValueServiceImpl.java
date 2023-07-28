package com.xqy.gulimall.product.service.impl;

import com.xqy.gulimall.product.vo.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.product.dao.SkuSaleAttrValueDao;
import com.xqy.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.xqy.gulimall.product.service.SkuSaleAttrValueService;


/**
 * impl sku出售attr价值服务
 *
 * @author xqy
 * @date 2023/07/28
 */
@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
        return this.baseMapper.getSaleAttrsBySpuId(spuId);

    }

}