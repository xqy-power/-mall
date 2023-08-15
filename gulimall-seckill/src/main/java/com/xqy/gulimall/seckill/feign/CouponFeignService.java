package com.xqy.gulimall.seckill.feign;

import com.xqy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 优惠券假装服务
 *
 * @author xqy
 * @date 2023/08/15
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/lates3DaySession")
    public R getLates3DaysSession();
}
