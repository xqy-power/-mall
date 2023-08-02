package com.xqy.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.xqy.gulimall.product.entity.ProductAttrValueEntity;
import com.xqy.gulimall.product.service.ProductAttrValueService;
import com.xqy.gulimall.product.vo.AttrRespVo;
import com.xqy.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xqy.gulimall.product.service.AttrService;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.R;


/**
 * 商品属性
 *
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-05 20:39:51
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;


    /**
     * 更新spu attr
     *
     * @param spuId    spu id
     * @param entities 实体
     * @return {@link R}
     *////product/attr/update/{spuId}
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId , @RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateSpuAttr(spuId , entities);
        return R.ok();
    }

    /**
     * spu基地attr列表
     *
     * @param spuId spu id
     * @return {@link R}
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListForSpu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> data = productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data", data);
    }

    /**
     * 基地attr列表
     *
     * @param params    参数个数
     * @param catelogId catelog id
     * @param type      类型
     * @return {@link R}
     */
//    @GetMapping("/base/list/{catelogId}")
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId, @PathVariable("attrType") String type) {
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, type);

        return R.ok().put("page", page);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    // @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrRespVo attrRespVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attrRespVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateAttr(attr);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
