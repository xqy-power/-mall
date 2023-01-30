package com.xqy.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.xqy.gulimall.product.entity.AttrEntity;
import com.xqy.gulimall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * attr集团attrs签证官
 *
 * @author xqy
 * @date 2022/12/31
 */
@Data
public class AttrGroupWithAttrsVo{

    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /**
     * attrs
     */
    private List<AttrEntity> attrs;

}
