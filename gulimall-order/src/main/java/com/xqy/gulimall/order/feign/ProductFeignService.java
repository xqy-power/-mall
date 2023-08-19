package com.xqy.gulimall.order.feign;

import com.xqy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 产品装服务
 *
 * @author xqy
 * @date 2023/08/09
 */

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/skuId/{id}")
    public R getSpuInfoBySkuId(@PathVariable("id") Long skuId);

    @GetMapping("/product/skuinfo/skuId")
    public R getSkuInfoBySkuId(@RequestParam Long skuId);
}
