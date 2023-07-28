package com.xqy.gulimall.product.web;

import com.xqy.gulimall.product.service.SkuInfoService;
import com.xqy.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * @author xqy
 */
@Controller
public class itemController {

    @Autowired
    SkuInfoService skuInfoService;
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId , Model model) throws ExecutionException, InterruptedException {
        System.out.println("准备查询"+skuId+"详情页");
        SkuItemVo vo = skuInfoService.item(skuId);
        System.out.println(vo);
        model.addAttribute("item",vo);
        return "item";
    }
}
