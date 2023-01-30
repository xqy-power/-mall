package com.xqy.gulimall.product.feign;

import com.xqy.common.to.SkuReductionTo;
import com.xqy.common.to.SpuBoundTo;
import com.xqy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 优惠券假装服务
 *
 * @author xqy
 * @date 2023/01/04
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 保存spu界限
     *spuBoundsService.save(spuBoundTo);
     *   1.@RequestBody 讲对象转化为JSON
     *   2.找到gulimall-coupon服务 ， 给/coupon/spubounds/save发送请求。
     *      将上一步的JSON放在请求体的位置发送请求
     *   3.对方服务收到请求，请求体里有JSON数据
     *      @RequestBody SpuBoundTo spuBoundTo  将请求体转化为SpuBoundTo；
     * 只要JSON的数据模型是兼容的，双方服务无需使用同一个to
     * @param spuBoundTo spu绑定到
     * @return {@link R}
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    /**
     * 保存sku减少
     *
     * @param skuReductionTo sku减少
     * @return
     */
    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(SkuReductionTo skuReductionTo);
}
