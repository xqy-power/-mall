package com.xqy.gulimall.product.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.xqy.gulimall.product.service.CategoryBrandRelationService;
import com.xqy.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
import org.springframework.util.StringUtils;


@Service("categoryService")
@EnableTransactionManagement
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    //    @Autowired
//    CategoryDao categoryDao;
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

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
     *1.同时进行多种缓存操作 @Caching
     * 2.指定删除某个分区下的所有数据 @CacheEvict(value = "category", allEntries = true)
     * 3.存储同一类型的数据，都可以指定为同一个分区。分区名默认就是缓存的前缀
     * @param category 类别
     */
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatelogJson'")
//    })
    //category:catelogJson
    @CacheEvict(value = "category", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    //代表当前的结果需要缓存，如果缓存中有，方法不用调用。如果缓存中没有，会调用方法，最后将结果放入缓存中。
    //key值 #root.method.name 使用spel表达式，来制定key值
    @Cacheable(cacheNames = {"category"}, key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(
                new QueryWrapper<CategoryEntity>()
                        .eq("parent_cid", 0)
        );
    }

    //TODO 产生堆外内存溢出   OutOfDirectMemoryError

    /**
     * 1)、springboot.2.g以后默认使用lettuce作为操作redis的客户端。它使用nettyi进行网络通信。
     * 2)、lettuce的bug导致nett惟外内存溢出 -Xmx300m   netty如果没有设置堆外内存 会默认使用你项目的内存 -Xmx300m
     * 可以通过-DIo。netty.maxDirectMemory  去调大堆外内存
     * 解决方案：
     * 1. 升级lettuce客户端    1.切换使用jedis
     *
     * @return
     */
    @Cacheable(cacheNames = {"category"}, key = "#root.method.name")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        //查询全部的分类数据 ，之后stream流filter过滤找到相应的几级分类 封装数据
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //1.查出所有的一级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);
        //封装数据、
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每个一级分类，查找每个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找当前二级分类下的三级分类；
                    List<CategoryEntity> level3Catelog = getParentCid(selectList, l2.getCatId());
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

    //    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson2() {
        /**
         * 1.空结果缓存 ， 解决缓存穿透
         * 2、设置过期时间（加随机值） ， 解决缓存雪崩
         * 3.加锁 ， 解决缓存击穿
         */
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            System.out.println("缓存不命中........查询数据库。。。。。。。");
            //缓存中没有  查询数据库
            Map<String, List<Catelog2Vo>> catelogJsonFormDb = getCatelogJsonFormDbWithRedissonLock();

            return catelogJsonFormDb;

        }
        System.out.println("缓存命中........直接返回。。。。。。。");

        //转为我们指定的对象
        return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    /**
     * 使用Redisson组件加锁
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFormDbWithRedissonLock() {
        RLock lock = redissonClient.getLock("cateJson-lock");
        lock.lock(5, TimeUnit.SECONDS);
        Map<String, List<Catelog2Vo>> dataFromDB = null;

        try {
            dataFromDB = getDataFromDB();
        } catch (Exception ignored) {

        } finally {
            lock.unlock();
        }

        return dataFromDB;

    }

    /**
     * 数据库优化，之前查找一级分类，二级分类，三级分类，全都需要去数据库中查找，性能差。
     * 现在只需要数据库查找一次全部的分类，之后在封装一级二级三级分类时  直接stream流filter操作过滤  、
     * 减少业务逻辑操作数据库的次数
     *
     * @return
     */
    /**
     * 使用Redis占坑的方式加锁
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFormDbWithRedisLock() {
        /** 加锁
         * 1. 单机版 使用synchronized（this）{代码块} ， springboot所有的组件在容器中都是单例的，可以使用
         *
         * 2.分布式版 ， Redisson 完成分布式锁
         */
        //占分布式锁 ， 去Redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        Map<String, List<Catelog2Vo>> dataFromDB = null;
        if (Boolean.TRUE.equals(lock)) {
            //加锁成功
            try {
                dataFromDB = getDataFromDB();
            } finally {
                //获取值对比+对比成功=原子操作  lua脚本
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //原子删锁
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }

//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)) {
//                //删除我自己的锁
//                stringRedisTemplate.delete("lock");  //删除锁
//            }
            return dataFromDB;
        } else {
            //加锁失败。。重试synchronized
            //休眠100ms
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
            return getCatelogJsonFormDbWithRedisLock(); //自旋的方式

        }
    }

    /**
     * 具体从数据库查询的代码
     *
     * @return
     */
    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        System.out.println("正在查询数据库............");


        //查询全部的分类数据 ，之后stream流filter过滤找到相应的几级分类 封装数据
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //1.查出所有的一级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);
        //封装数据、
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每个一级分类，查找每个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找当前二级分类下的三级分类；
                    List<CategoryEntity> level3Catelog = getParentCid(selectList, l2.getCatId());
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
        //将查到的数据放入缓存中 ， 讲对象转为JSON在放在缓存
//        String s = JSON.toJSONString(catelogJsonFormDb);
        String s = JSON.toJSONString(parentCid);
        //判断空的缓存结果为 0  ， 并加入过期时间使用Random随机值
        Random random = new Random();
        int i = (random.nextInt(5)) + 1;
        stringRedisTemplate.opsForValue().set("catalogJSON", s.isEmpty() ? "0" : s, i, TimeUnit.DAYS);
        return parentCid;
    }

    /**
     * 使用本地锁的方式测试
     *
     * @return
     */
    public synchronized Map<String, List<Catelog2Vo>> getCatelogJsonFormDbWithLocalLock() {
        /** 加锁
         * 1. 单机版 使用synchronized（this）{代码块} ， springboot所有的组件在容器中都是单例的，可以使用
         *
         * 2.分布式版 ， Redisson 完成分布式锁
         */

        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (catalogJSON != null) {
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        System.out.println("正在查询数据库............");


        //查询全部的分类数据 ，之后stream流filter过滤找到相应的几级分类 封装数据
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        //1.查出所有的一级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);
        //封装数据、
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //每个一级分类，查找每个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //找当前二级分类下的三级分类；
                    List<CategoryEntity> level3Catelog = getParentCid(selectList, l2.getCatId());
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
        //将查到的数据放入缓存中 ， 讲对象转为JSON在放在缓存
//        String s = JSON.toJSONString(catelogJsonFormDb);
        String s = JSON.toJSONString(parentCid);
        //判断空的缓存结果为 0  ， 并加入过期时间使用Random随机值
        Random random = new Random();
        int i = (random.nextInt(5)) + 1;
        stringRedisTemplate.opsForValue().set("catalogJSON", s.isEmpty() ? "0" : s, i, TimeUnit.DAYS);
        return parentCid;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList, Long parent_cid) {
//        return baseMapper.selectList(
//                new QueryWrapper<CategoryEntity>()
//                        .eq("parent_cid", v.getCatId())
//        );
        return selectList.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
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