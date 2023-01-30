package com.xqy.gulimall.product.vo;

import lombok.Data;

/**
 * attr resp签证官
 *
 * @author xqy
 * @date 2022/12/18
 */
@Data
public class AttrRespVo extends AttrVo {
    private String catelogName;

    private String groupName;

    private Long[] catelogPath;


}
