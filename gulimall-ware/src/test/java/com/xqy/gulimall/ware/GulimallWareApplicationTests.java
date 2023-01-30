package com.xqy.gulimall.ware;


import com.xqy.gulimall.ware.service.WareSkuService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallWareApplicationTests {

    @Autowired
    WareSkuService wareSkuService;
    @Test
    public void contextLoads() {
        wareSkuService.addStock(44L , 4L , 34);
    }

}
