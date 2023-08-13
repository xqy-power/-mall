package com.xqy.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.xqy.gulimall.order.config.AlipayTemplate;
import com.xqy.gulimall.order.service.OrderService;
import com.xqy.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 支付网络控制器
 *
 * @author xqy
 * @date 2023/08/12
 */
@Controller
public class PayWebController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    /**
     * 支付订单
     *支付成功之后，我们要跳转到订单列表页
     * @param orderSn 订单sn
     * @return {@link String}
     * @throws AlipayApiException 支付宝api例外
     */
    @ResponseBody
    @GetMapping(value = "/payOrder" ,produces = "text/html")
    public String payOrder(@RequestParam("OrderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
        System.out.println("调用支付宝支付。。。。。。"+pay);
        return pay;
    }
}
