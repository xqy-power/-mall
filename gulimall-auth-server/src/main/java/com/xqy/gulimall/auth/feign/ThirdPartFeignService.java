package com.xqy.gulimall.auth.feign;

import com.xqy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 第三部分装服务
 *
 * @author xqy
 * @date 2023/07/29
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartFeignService {

    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
