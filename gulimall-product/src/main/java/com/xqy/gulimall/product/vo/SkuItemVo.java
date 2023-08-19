package com.xqy.gulimall.product.vo;

import com.xqy.gulimall.product.entity.SkuImagesEntity;
import com.xqy.gulimall.product.entity.SkuInfoEntity;
import com.xqy.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author xqy
 */
@Data
public class SkuItemVo {

    //1.获取sku基本信息  pms_sku_info

    SkuInfoEntity info;
    //2.获取sku图片信息  pms_sku_images

    boolean hadStock = true;

    List<SkuImagesEntity> images;
    //3.获得当前spu的所有销售属性组合

    List<SkuItemSaleAttrVo> saleAttr;
    //4.获得spu的介绍
    SpuInfoDescEntity desp;
    //5.获得spu规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    //6.秒杀信息
    SeckillInfoVo seckillInfoVo;

//    @Data
//    public static class SkuItemSaleAttrVo {
//        private Long attrId;
//
//        private String attrName;
//
//        private List<String> attrValues;
//    }

//    @Data
//    @ToString
//    public static class SpuItemAttrGroupVo {
//        private String groupName;
//
//        private List<SpuBaseAttrVo> attrs;
//
//    }
//
//    @Data
//    @ToString
//    public static class SpuBaseAttrVo {
//        private String attrName;
//        private String attrValue;
//    }
}
