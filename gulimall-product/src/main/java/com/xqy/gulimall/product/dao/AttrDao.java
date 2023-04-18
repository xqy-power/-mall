package com.xqy.gulimall.product.dao;

import com.xqy.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品属性
 * 
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-05 20:06:32
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {


    List<Long> selectSearchAttrIds(@Param("attrIds") List<Long> attrIds);
}
