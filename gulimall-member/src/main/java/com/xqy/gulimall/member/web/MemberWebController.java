package com.xqy.gulimall.member.web;

import com.alibaba.fastjson2.JSON;
import com.xqy.common.utils.R;
import com.xqy.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * 成员网络控制器
 *
 * @author xqy
 * @date 2023/08/12
 */
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrder(@RequestParam(value = "pageNum" ,defaultValue = "1") Integer pageNum , Model model) {
        //获取支付宝给我们传来 的所有请求数据
        //验证签名，我们只需要调用sdk的verify方法即可

        //查出当前登录用户的所有订单列表数据
        Map<String, Object> page = new HashMap<>();
        page.put("page", pageNum.toString());

        R r = orderFeignService.listWithItem(page);
        model.addAttribute("orders", r);
        System.out.println(JSON.toJSONString(r));
        return "orderList";
    }
}
