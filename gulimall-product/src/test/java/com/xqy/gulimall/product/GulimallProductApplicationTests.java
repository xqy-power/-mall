package com.xqy.gulimall.product;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xqy.gulimall.product.entity.BrandEntity;
import com.xqy.gulimall.product.service.BrandService;
import com.xqy.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Test
    public void pathTest() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.error("路径数组：{}" , Arrays.asList(catelogPath));
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
