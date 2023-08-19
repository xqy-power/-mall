package com.xqy.gulimall.product.service.impl;

import com.alibaba.fastjson2.TypeReference;
import com.xqy.common.utils.R;
import com.xqy.gulimall.product.entity.SkuImagesEntity;
import com.xqy.gulimall.product.entity.SpuInfoDescEntity;
import com.xqy.gulimall.product.feign.SeckillFeignService;
import com.xqy.gulimall.product.service.*;
import com.xqy.gulimall.product.vo.SeckillInfoVo;
import com.xqy.gulimall.product.vo.SkuItemSaleAttrVo;
import com.xqy.gulimall.product.vo.SkuItemVo;
import com.xqy.gulimall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.product.dao.SkuInfoDao;
import com.xqy.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            wrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            BigDecimal bigDecimal = new BigDecimal(max);
            if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                wrapper.le("price", max);
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(
                new QueryWrapper<SkuInfoEntity>()
                        .eq("spu_id", spuId)
        );

        return list;

    }

    /**
     * 使用线程池CompletableFuture异步编排方式提高效率
     *
     * @param skuId sku id
     * @return {@link SkuItemVo}
     * @throws ExecutionException   执行异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuitemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1.获取sku基本信息  pms_sku_info
            SkuInfoEntity info = getById(skuId);
            skuitemVo.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //3.获得当前spu的所有销售属性组合
            List<SkuItemSaleAttrVo> skuItemSaleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuitemVo.setSaleAttr(skuItemSaleAttrVos);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            //4.获得spu的介绍
            SpuInfoDescEntity spuInfoDescServiceById = spuInfoDescService.getById(res.getSpuId());
            skuitemVo.setDesp(spuInfoDescServiceById);
        }, executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //5.获得spu规格参数信息
            List<SpuItemAttrGroupVo> spuItemAttrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuitemVo.setGroupAttrs(spuItemAttrGroupVos);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //2.获取sku图片信息  pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuitemVo.setImages(images);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            //3.查询当前sku是否参与秒杀服务
            try {
                R r = seckillFeignService.getSkuSeckillInfo(skuId);
                if (r.getCode() == 0) {
                    SeckillInfoVo seckillInfoVo = r.getData(new TypeReference<SeckillInfoVo>() {
                    });
                    skuitemVo.setSeckillInfoVo(seckillInfoVo);
                }
            } catch (Exception e) {
                System.out.println("远程调用秒杀服务失败");
            }
        },executor);

        //等待所有任务完成
        CompletableFuture.allOf(infoFuture,saleAttrFuture,descFuture,baseAttrFuture,imageFuture,seckillFuture).get();

        return skuitemVo;
    }

    @Override
    public SkuInfoEntity getSkuInfoBySkuId(Long skuId) {
        return this.baseMapper.selectOne(new QueryWrapper<SkuInfoEntity>().eq("sku_id", skuId));
    }

}