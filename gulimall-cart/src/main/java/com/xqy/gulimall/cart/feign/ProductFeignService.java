package com.xqy.gulimall.cart.feign;

import com.xqy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * 产品装服务
 *
 * @author xqy
 * @date 2023/08/01
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);


    @GetMapping("/product/skusaleattrvalue/Stringlist/{skuId}")
    public List<String> getSkuSaleAttrValues(@PathVariable Long skuId);

    @GetMapping("/product/skuinfo/{skuId}/price")
    public BigDecimal getPrice(@PathVariable Long skuId);
}
