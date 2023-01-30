package com.xqy.gulimall.product.service.impl;

import com.xqy.gulimall.product.service.CategoryBrandRelationService;
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
        }).map(categoryEntity->{
            categoryEntity.setChildren(GetChildrens( categoryEntity , entities));
            return categoryEntity;
        }).sorted((menu1 , menu2)->{
            return menu1.getSort()- menu2.getSort();
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
        categoryBrandRelationService.updateCategory(category.getCatId() , category.getName());
    }

    /**
     * 找到父路径
     *
     * @param catelogId catelog id
     * @param paths     路径
     * @return {@link List}<{@link Long}>
     */
    private List<Long> findParentPath(Long catelogId , List<Long> paths){
        //1.放入当前路径
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);

        if (byId.getParentCid()!= 0 ) {
            findParentPath(byId.getParentCid() , paths);
        }
        return paths;
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param current_menu     当前菜单
     * @param entities 所有实体
     * @return {@link List}<{@link CategoryEntity}>
     */
    private List<CategoryEntity> GetChildrens( CategoryEntity current_menu , List<CategoryEntity> entities ){

        List<CategoryEntity> collect = entities.stream().filter(categoryEntity -> {
            //得到2级菜单
            return categoryEntity.getParentCid().equals(current_menu.getCatId());
        }).map(categoryEntity -> {
            //递归得到3级菜单
            categoryEntity.setChildren(GetChildrens( categoryEntity , entities));
            return categoryEntity;
        }).sorted((menu1 , menu2)->{
            return (menu1.getSort()==null?0: menu1.getSort())-(menu2.getSort()==null?0: menu2.getSort());
        }).collect(Collectors.toList());
        return collect;
    }

}