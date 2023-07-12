package com.xqy.gulimall.search.vo;

import com.xqy.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @author xqy
 */
@Data
public class SearchResult {
    // 查询到的所有商品信息
    private List<SkuEsModel> products;

    // 分页信息
    private Integer pageNum; // 当前页码
    private Long total; // 总记录数
    private Integer totalPages; // 总页码
    private List<Integer> pageNavs; // 导航页码

    // 品牌信息
    private List<BrandVo> brands; // 当前查询到的结果，所有涉及到的品牌

    // 属性信息
    private List<AttrVo> attrs; // 当前查询到的结果，所有涉及到的所有属性

    // 分类信息
    private List<CatalogVo> catalogs; // 当前查询到的结果，所有涉及到的所有分类

    //=========以上是返回给页面的所有信息==========


    //面包屑导航功能
    private List<NavVo> navs;

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class AttrVo {
        private Long attrId; // 属性id
        private String attrName; // 属性名
        private List<String> attrValue; // 属性值

    }
    @Data
    public static class BrandVo {
        private Long brandId; // 品牌id
        private String brandName; // 品牌名
        private String brandImg; // 品牌图片

    }


    @Data
    public static class CatalogVo {
        private Long catalogId; // 分类id
        private String catalogName; // 分类名
    }

}
