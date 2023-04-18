package com.xqy.gulimall.search.controller;

import com.xqy.common.exception.BizCodeEnume;
import com.xqy.common.to.es.SkuEsModel;
import com.xqy.common.utils.R;
import com.xqy.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xqy
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels)  {
        Boolean b =false;
        try {
            b = productSaveService.productStatusUp(skuEsModels);
        }catch (Exception e){
            log.error("ElasticSaveController商品上架出错:{ }" , e);
        }
        if (!b) {
            return R.ok();
        }else {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}
