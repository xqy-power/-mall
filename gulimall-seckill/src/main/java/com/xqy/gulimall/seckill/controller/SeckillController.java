package com.xqy.gulimall.seckill.controller;

import com.xqy.common.utils.R;
import com.xqy.gulimall.seckill.service.SeckillService;
import com.xqy.gulimall.seckill.to.SeckillRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 秒杀控制器
 *
 * @author xqy
 * @date 2023/08/15
 */

@RestController
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    /**
     * 得到当前秒杀sku
     *
     * @return {@link R}
     */
    @GetMapping("/getCurrentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SeckillRedisTo> vos = seckillService.getCurrentSeckillSkus();
        System.out.println(vos);
        return R.ok().setData(vos);
    }
}
