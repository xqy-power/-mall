package com.xqy.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.xqy.gulimall.product.entity.AttrEntity;
import com.xqy.gulimall.product.service.AttrAttrgroupRelationService;
import com.xqy.gulimall.product.service.AttrService;
import com.xqy.gulimall.product.service.CategoryService;
import com.xqy.gulimall.product.vo.AttrGroupRelationVo;
import com.xqy.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.xqy.gulimall.product.entity.AttrGroupEntity;
import com.xqy.gulimall.product.service.AttrGroupService;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.R;



/**
 * 属性分组
 *
 * @author xieqianyu
 * @email 119596909@qq.com
 * @date 2022-12-05 20:39:51
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrService attrService;

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 添加关系
     *
     * @param vos vos
     * @return {@link R}
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos){
        attrAttrgroupRelationService.saveBatch(vos);
        return R.ok();
    }

    /**
     * 能与attrs attr集团
     *
     * @param catelogId catelog id
     * @return {@link R}
     *///    /product/attrgroup/{catelogId}/withattr
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs( @PathVariable("catelogId") Long catelogId){
        //1.查出当前分类下的所有属性分组
        //2.查出每个属性分组下的所有属性
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatlogId(catelogId);
        return R.ok().put("data" , vos);

    }



    /**
     * 根据分组id查找所有的没有关联属性
     *
     * @param attrgroupId attrgroup id
     * @param params      参数个数
     * @return {@link R}
     *///    http://localhost:88/api/product/attrgroup/1/noattr/relation?t=1672384226776&page=1&limit=10&key=
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId
            ,@RequestParam Map<String, Object> params){
        PageUtils page =  attrService.getNoRelationAttr(params,attrgroupId);
        return R.ok().put("page" , page);
    }

    /**
     * 根据分组id查找所有的关联属性
     *
     * @param attrgroupId attrgroup id
     * @return {@link R}
     *///    http://localhost:88/api/product/attrgroup/1/attr/relation?t=1672380465884
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> attrEntities =  attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data" , attrEntities);
    }


    /**
     * 删除关系
     *
     * @param vos vos
     * @return {@link R}
     *///    http://localhost:88/api/product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos){
        attrService.deleteRelation(vos);
        return R.ok();

    }
    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
   // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params ,@PathVariable("catelogId") Long catelogId){
//        PageUtils page = attrGroupService.queryPage(params);

        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
   // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);


        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
   // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
   // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
