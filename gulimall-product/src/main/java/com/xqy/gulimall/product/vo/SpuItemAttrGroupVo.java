package com.xqy.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author xqy
 */
@Data
@ToString
public class SpuItemAttrGroupVo {
    private String groupName;

    private List<SpuBaseAttrVo> attrs;
}
