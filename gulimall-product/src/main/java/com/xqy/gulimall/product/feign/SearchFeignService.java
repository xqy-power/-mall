package com.xqy.gulimall.product.feign;

import com.xqy.common.to.es.SkuEsModel;
import com.xqy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author xqy
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {
    @PostMapping("/search/product" )
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
