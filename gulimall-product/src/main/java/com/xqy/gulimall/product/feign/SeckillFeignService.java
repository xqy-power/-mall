package com.xqy.gulimall.product.feign;

import com.xqy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 秒杀假装服务
 *
 * @author xqy
 * @date 2023/08/15
 */
@FeignClient("gulimall-seckill")
public interface SeckillFeignService {
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable Long skuId);
}
