package com.xqy.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.xqy.gulimall.ware.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xqy.gulimall.ware.entity.WareSkuEntity;
import com.xqy.gulimall.ware.service.WareSkuService;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.R;


/**
 * 商品库存
 *
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-06 11:01:33
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    //查询sku是否有库存
    @PostMapping("/hasstock")
    public R<List<SkuHasStockVo>> getSkusHasStock(@RequestBody List<Long> skuIds) {
        //skuID stock
        List<SkuHasStockVo> vos =  wareSkuService.getSkusHasStock(skuIds);
        R<List<SkuHasStockVo>> ok = R.ok();
        ok.setData(vos);
        return ok;
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
