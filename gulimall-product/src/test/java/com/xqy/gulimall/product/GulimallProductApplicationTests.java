package com.xqy.gulimall.product;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xqy.gulimall.product.dao.AttrGroupDao;
import com.xqy.gulimall.product.dao.SkuSaleAttrValueDao;
import com.xqy.gulimall.product.entity.BrandEntity;
import com.xqy.gulimall.product.service.BrandService;
import com.xqy.gulimall.product.service.CategoryService;
import com.xqy.gulimall.product.vo.SkuItemSaleAttrVo;
import com.xqy.gulimall.product.vo.SkuItemVo;
import com.xqy.gulimall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;
    @Test
    public void attrGroupDap(){
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(100L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }
    @Test
    public void attrSaleDao(){
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(42L);
        System.out.println(saleAttrsBySpuId);
    }
    @Test
    public void redisson() {
        System.out.println(redissonClient);
    }


    @Test
    public void pathTest() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("people" , "xqy_"+ UUID.randomUUID().toString());
//        Long[] catelogPath = categoryService.findCatelogPath(225L);
//        log.error("路径数组：{}" , Arrays.asList(catelogPath));
    }
    @Test
    public void pathTest2() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String people = ops.get("people");
        System.out.println(people);
//        Long[] catelogPath = categoryService.findCatelogPath(225L);
//        log.error("路径数组：{}" , Arrays.asList(catelogPath));
    }



    @Test
    public void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(Long.valueOf(13));
//        brandEntity.setName("京东");
//        brandService.updateById(brandEntity);
//        System.out.println("修改成功！！！！");

        List<BrandEntity> brandId = brandService.list(new QueryWrapper<BrandEntity>().ge("brand_id", 11));
        System.out.println(brandId);
    }

}
