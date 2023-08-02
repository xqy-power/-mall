package com.xqy.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * sku项目销售attr签证官
 *
 * @author xqy
 * @date 2023/07/28
 */
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;

    private String attrName;

    private List<AttrValueWithAkuIdVo> attrValues;
}
