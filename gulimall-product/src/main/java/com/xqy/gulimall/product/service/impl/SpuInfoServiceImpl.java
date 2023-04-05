package com.xqy.gulimall.product.service.impl;

import com.alibaba.fastjson2.TypeReference;
import com.xqy.common.constant.ProductConstant;
import com.xqy.common.to.SkuReductionTo;
import com.xqy.common.to.SpuBoundTo;
import com.xqy.common.to.es.SkuEsModel;
import com.xqy.common.utils.R;
import com.xqy.gulimall.product.entity.*;
import com.xqy.gulimall.product.feign.CouponFeignService;
import com.xqy.gulimall.product.feign.SearchFeignService;
import com.xqy.gulimall.product.feign.WareFeignService;
import com.xqy.gulimall.product.service.*;
import com.xqy.gulimall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author xqy
 */
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {


    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    AttrService attrService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * spu保存信息
     *
     * @param vo 签证官
     */
    @Override
//    @Transactional(readOnly = false)
    public void saveSpuInfo(SpuSaveVo vo) {
//        1.保存spu基本信息  pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        infoEntity.setPublishStatus(vo.getPublishStatus());
        System.out.println(infoEntity);
        this.saveBaseSpuInfo(infoEntity);

//        2.保存spu的描述   pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity infoDescEntity = new SpuInfoDescEntity();
        infoDescEntity.setSpuId(infoEntity.getId());
        infoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(infoDescEntity);

//        3.保存spu的图片集  pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(infoEntity.getId(), images);

//        4.保存spu的规格参数  pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(item.getAttrId());
            AttrEntity byId = attrService.getById(item.getAttrId());
            productAttrValueEntity.setAttrName(byId.getAttrName());
            productAttrValueEntity.setAttrValue(item.getAttrValues());
            productAttrValueEntity.setQuickShow(item.getShowDesc());
            productAttrValueEntity.setSpuId(infoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);

//        5.保存spu的积分信息  gulimall-sms ==》 sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0 ){
            log.error("远程保存spu积分信息出错");
        }


//        6.保存当前spu对应的所有sku信息
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(sku -> {
                String defaultImage = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImage = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoDescEntity.getSpuId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                //        6.1)  保存sku的基本信息  pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> collectImage = sku.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
//                    返回TRUE就是需要 ， 返回false就是去除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());

                //        6.2)  保存sku的图片信息  pms_sku_images
                //TODO 没有图片路径的无需保存
                skuImagesService.saveBatch(collectImage);

                List<Attr> attrList = sku.getAttr();
                List<SkuSaleAttrValueEntity> collectSkuSaleAttr = attrList.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());

                //        6.3)  保存sku的销售属性  pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(collectSkuSaleAttr);


//        6.4)  sku的优惠满减信息  gulimall-sms ==》 sms_sku_ladder 、 sms_sku_full_reduction ， sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0 ){
                        log.error("远程保存sku优惠信息出错");
                    }
                }
            });
        }

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        baseMapper.insert(infoEntity);

    }

    /**
     * 通过条件查询页面
     *
     * @param params 参数个数
     * @return {@link PageUtils}
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String)params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("id" , key).or().like("spu_name"  , key);
            });
        }

        String status = (String)params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status" , status );
        }

        String brandId = (String)params.get("brandId");
        if (!StringUtils.isEmpty(brandId)  && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id" , brandId );
        }

        String catelogId = (String)params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId)  && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id" , catelogId );
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     *  商品上架
     * @param spuId
     */
    @Override
    public void up(Long spuId) {
        //1.组装需要的数据
        //  查出当前spuId对应的所有的sku信息 品牌的名字
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //TODO 4,。查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> attrValueEntities = productAttrValueService.baseAttrListForSpu(spuId);

        List<Long> attrIds = attrValueEntities.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        List<Long> searchAttrId = attrService.selectSearchAttrIds(attrIds);

        HashSet<Long> idSet = new HashSet<>(searchAttrId);

        List<SkuEsModel.Attrs> attrsList = attrValueEntities.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        //TODO 1.发送远程称调用 库存系统查询费否有库存
        Map<Long, Boolean> stockMap =null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIds);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {
            };
            stockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        }catch (Exception e) {
            log.error("库存服务查询异常：原因{}" ,e);

        }

        //封装每一个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skuInfoEntities.stream().map(sku -> {
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            //设置库存信息
            if (finalStockMap ==null) {
                esModel.setHasStock(true);
            }else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            //TODO 2. 热度评分 ， 默认0
            esModel.setHotScore(0L);

            //TODO 3. 查询品牌和分类的名字信息\
            BrandEntity byId = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(byId.getName());
            esModel.setBrandImg(byId.getLogo());

            CategoryEntity category = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(category.getName());
            //设置检索属性
            esModel.setAttrs(attrsList);


            return esModel;
        }).collect(Collectors.toList());

        //TODO 5. 将数据发送给给ES进行保存 guilimall-serch
        R r = searchFeignService.productStatusUp(upProducts);
        if(r.getCode() == 0){
            //远程调用成功
            //TODO 6.修改点前spu的状态
            this.baseMapper.updateSpuStatus(spuId , ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            //远程调用失败
            //TODO 7.重复调用？ 接口幂等性; 重试机制
            //feign调用流程
            /**
             * 1.构造请求数据 ， 讲对象转换成json
             * 2。发送请求进行执行
             * 3.执行请求会有重试机制
             */
        }
    }
}