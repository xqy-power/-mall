package com.xqy.gulimall.order.feign;

import com.xqy.common.utils.R;
import com.xqy.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 车装服务
 *
 * @author xqy
 * @date 2023/08/07
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {
    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<OrderItemVo> getCurrentUserCartItems();
}
