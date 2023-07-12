package com.xqy.gulimall.product.service.impl;

import com.sun.xml.internal.bind.v2.TODO;
import com.xqy.gulimall.product.dao.CategoryBrandRelationDao;
import com.xqy.gulimall.product.entity.AttrGroupEntity;
import com.xqy.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.product.dao.BrandDao;
import com.xqy.gulimall.product.entity.BrandEntity;
import com.xqy.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;


/**
 * 品牌服务impl
 *
 * @author xqy
 * @date 2022/12/18
 */
@Service("brandService")
@EnableTransactionManagement
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and((obj) -> {
                obj.eq("brand_id", key)
                        .or().like("name", key)
                        .or().like("descript", key);
            });
            IPage<BrandEntity> page = this.page(
                    new Query<BrandEntity>().getPage(params),
                    queryWrapper
            );
            return new PageUtils(page);
        }else {
            IPage<BrandEntity> page = this.page(
                    new Query<BrandEntity>().getPage(params),
                    new QueryWrapper<BrandEntity>()
            );
            return new PageUtils(page);
        }

    }

    /**
     * 更新详细信息  所有的品牌信息
     *  保证其他表中冗余字段的一致性。
     * @param brand 品牌
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(BrandEntity brand) {
        this.baseMapper.updateById(brand);
        if (StringUtils.isNotEmpty(brand.getName())) {
            //同步更改其他表中的品牌名称
            categoryBrandRelationService.updateBrand(brand.getBrandId() , brand.getName());

            //TODO 更新其他关联表信息

        }
    }

    @Override
    @Cacheable(value = "brand",key = "#root.methodName",sync = true)
    public List<BrandEntity> getBrandsByIds(List<Long> brandId) {
        return this.baseMapper.selectBatchIds(brandId);
    }

}