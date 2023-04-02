package com.xqy.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xqy.common.utils.PageUtils;
import com.xqy.gulimall.product.entity.AttrEntity;
import com.xqy.gulimall.product.vo.AttrGroupRelationVo;
import com.xqy.gulimall.product.vo.AttrRespVo;
import com.xqy.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-05 20:06:32
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVo[] vos);

    /**
     * 没有关系attr
     *
     * @param params      参数个数
     * @param attrgroupId attrgroup id
     * @return {@link PageUtils}
     */
    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    /**
     * 在指定多有属性集合里，挑出检索属性
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

