package com.xqy.gulimall.seckill.controller;

import com.xqy.common.utils.R;
import com.xqy.gulimall.seckill.service.SeckillService;
import com.xqy.gulimall.seckill.to.SeckillRedisTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀控制器
 *
 * @author xqy
 * @date 2023/08/15
 */

@Controller
@Slf4j
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    /**
     * 得到当前秒杀sku
     *
     * @return {@link R}
     */
    @GetMapping("/getCurrentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus() {
        log.info("getCurrentSeckillSkus==========>正在执行");
        List<SeckillRedisTo> vos = seckillService.getCurrentSeckillSkus();
//        System.out.println(vos);
        return R.ok().setData(vos);
    }


    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSeckillInfo(@PathVariable Long skuId) {
        SeckillRedisTo redisTo = seckillService.getSkfuSeckillInfo(skuId);
        return R.ok().setData(redisTo);
    }

    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) throws InterruptedException {
        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
