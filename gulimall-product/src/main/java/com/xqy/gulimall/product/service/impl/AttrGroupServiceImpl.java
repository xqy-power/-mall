package com.xqy.gulimall.product.service.impl;

import com.xqy.gulimall.product.entity.AttrEntity;
import com.xqy.gulimall.product.service.AttrService;
import com.xqy.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.product.dao.AttrGroupDao;
import com.xqy.gulimall.product.entity.AttrGroupEntity;
import com.xqy.gulimall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询页面
     *
     *如果catelogId == 0 就是查询全部，在参数params中包含key就是模糊查询，
     * 查找attr_group_id 或者 attr_group_name 或者 descript  包含该key值的。
     * 如果如果catelogId ！= 0 就是查询该catelogId的属性分组，并判断是否需要模糊查询。
     *
     * @param params    参数个数
     * @param catelogId catelog id
     * @return {@link PageUtils}
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        if (catelogId == 0) {
            String key = (String) params.get("key");
            QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
            if (StringUtils.isNotEmpty(key)) {
                queryWrapper.and((obj) -> {
                    obj.eq("attr_group_id", key)
                            .or().like("attr_group_name", key)
                            .or().like("descript", key);
                });
                IPage<AttrGroupEntity> page = this.page(
                        new Query<AttrGroupEntity>().getPage(params),
                        queryWrapper
                );
                return new PageUtils(page);
            }else {
                IPage<AttrGroupEntity> page = this.page(
                        new Query<AttrGroupEntity>().getPage(params),
                        new QueryWrapper<AttrGroupEntity>()
                );
                return new PageUtils(page);
            }
        } else {
            String key = (String) params.get("key");
            QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>()
                    .eq("catelog_id", catelogId);
            if (StringUtils.isNotEmpty(key)) {
                queryWrapper.and((obj) -> {
                    obj.eq("attr_group_id", key)
                            .or().like("attr_group_name", key)
                            .or().like("descript", key);
                });
            }
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper
            );
            return new PageUtils(page);
        }
    }

    /**
     * 根据分类id 查出所有的分组以及分组的属性
     *
     * @param catelogId catelog id
     * @return {@link List}<{@link AttrGroupWithAttrsVo}>
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatlogId(Long catelogId) {
        //1.查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(
                new QueryWrapper<AttrGroupEntity>()
                        .eq("catelog_id", catelogId)
        );

        //根据分组查询所有分组的属性
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            List<AttrEntity> attr = attrService.getRelationAttr(item.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(attr);
            return attrGroupWithAttrsVo;

        }).collect(Collectors.toList());

        return collect;
    }

}