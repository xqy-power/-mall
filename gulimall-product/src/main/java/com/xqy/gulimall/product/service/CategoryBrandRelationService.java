package com.xqy.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xqy.common.utils.PageUtils;
import com.xqy.gulimall.product.entity.BrandEntity;
import com.xqy.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-05 20:06:32
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    /**
     * 更新类别
     *
     * @param catId 猫id
     * @param name  名字
     */
    void updateCategory(Long catId, String name);

    /**
     * 被猫品牌标识
     *
     * @param catId 猫id
     * @return {@link List}<{@link BrandEntity}>
     */
    List<BrandEntity> getBrandsByCatId(Long catId);
}

