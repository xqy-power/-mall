package com.xqy.gulimall.product.service.impl;

import com.xqy.gulimall.product.service.CategoryBrandRelationService;
import com.xqy.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xqy.common.utils.PageUtils;
import com.xqy.common.utils.Query;

import com.xqy.gulimall.product.dao.CategoryDao;
import com.xqy.gulimall.product.entity.CategoryEntity;
import com.xqy.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;



@Service("categoryService")
@EnableTransactionManagement
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    //    @Autowired
//    CategoryDao categoryDao;
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查出所有的分类以及子分类，以树形结构组装起来
     *
     * @return {@link List}<{@link CategoryEntity}>
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有的分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.组装成树形的结构
        //2.1 找到一级分类
        List<CategoryEntity> menu_list = entities.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == 0;
        }).map(categoryEntity -> {
            categoryEntity.setChildren(GetChildrens(categoryEntity, entities));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return menu1.getSort() - menu2.getSort();
        }).collect(Collectors.toList());

        return menu_list;
    }

    /**
     * 逻辑删除菜单由id
     *
     * @param asList 正如列表
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1.检查当前删除的菜单，是否在别的地方被引用

        //逻辑删除，
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 找到catelogId路径  [父/子/孙]
     *
     * @param catelogId 集团attr组id
     * @return {@link Long[]}
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category 类别
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(
                new QueryWrapper<CategoryEntity>()
                        .eq("parent_cid", 0)
        );

    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        //1.查出所有的一级分类
        List<CategoryEntity> level1Categorys = this.getLevel1Categorys();
        //封装数据、
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每个一级分类，查找每个一级分类的二级分类
            List<CategoryEntity> categoryEntities = baseMapper.selectList(
                    new QueryWrapper<CategoryEntity>()
                            .eq("parent_cid", v.getCatId())
            );
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找当前二级分类下的三级分类；
                    List<CategoryEntity> level3Catelog = baseMapper.selectList(
                            new QueryWrapper<CategoryEntity>()
                                    .eq("parent_cid", l2.getCatId())
                    );
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            return new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parentCid;
    }

    /**
     * 找到父路径
     *
     * @param catelogId catelog id
     * @param paths     路径
     * @return {@link List}<{@link Long}>
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1.放入当前路径
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);

        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param current_menu 当前菜单
     * @param entities     所有实体
     * @return {@link List}<{@link CategoryEntity}>
     */
    private List<CategoryEntity> GetChildrens(CategoryEntity current_menu, List<CategoryEntity> entities) {

        List<CategoryEntity> collect = entities.stream().filter(categoryEntity -> {
            //得到2级菜单
            return categoryEntity.getParentCid().equals(current_menu.getCatId());
        }).map(categoryEntity -> {
            //递归得到3级菜单
            categoryEntity.setChildren(GetChildrens(categoryEntity, entities));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return collect;
    }

}