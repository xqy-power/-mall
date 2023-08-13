package com.xqy.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xqy.common.to.mq.OrderTo;
import com.xqy.common.to.mq.StockLockedTo;
import com.xqy.common.utils.PageUtils;
import com.xqy.gulimall.ware.entity.WareSkuEntity;
import com.xqy.gulimall.ware.vo.SkuHasStockVo;
import com.xqy.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 器皿sku服务
 * 商品库存
 *
 * @author xieqianyu
 * @date 2022-12-06 11:01:33
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    void orderLockStock(WareSkuLockVo vo);

    void handleStockLockedRelease(StockLockedTo stockLockedTo);

    void handleStockLockedRelease(OrderTo orderTo);
}

