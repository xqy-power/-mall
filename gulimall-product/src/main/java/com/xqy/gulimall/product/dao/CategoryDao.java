package com.xqy.gulimall.product.dao;

import com.xqy.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-05 20:06:32
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
