package com.xqy.gulimall.order.web;

import com.xqy.common.exception.NoStockException;
import com.xqy.gulimall.order.service.OrderService;
import com.xqy.gulimall.order.vo.OrderConfirmVo;
import com.xqy.gulimall.order.vo.OrderSubmitVo;
import com.xqy.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

/**
 * 订单web控制器
 *
 * @author xqy
 * @date 2023/08/07
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 确认订单
     *
     * @param model   模型
     * @param request 请求
     * @return {@link String}
     * @throws ExecutionException   执行异常
     * @throws InterruptedException 中断异常
     */

    @GetMapping("/toTrade")
    public String toTrade(Model model , HttpServletRequest request ) throws ExecutionException, InterruptedException {
       OrderConfirmVo confirmVo =  orderService.confirmOrder();

       model.addAttribute("confirmOrderData",confirmVo);
        //展示订单确认页
        return "confirm";
    }


    /**
     * 提交订单
     *
     * @param submitVo 提交签证官
     * @param model    模型
     * @param request  请求
     * @return {@link String}
     */

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo submitVo, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request){

        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(submitVo);
            //下单成功来到支付选择页
            //下单失败回到订单确认页重新确认订单信息
            System.out.println(submitVo);
            if (responseVo.getCode() == 0) {
                model.addAttribute("SubmitOrderResp", responseVo);
                return "pay";
            }else {
                String msg = "下单失败，请重新提交订单";
                switch (responseVo.getCode()){
                    case 1: msg += "订单信息过期，请刷新再次提交";break;
                    case 2: msg += "订单商品价格发生变化，请确认后再次提交";break;
                    case 3: msg += "库存锁定失败，商品库存不足";break;
                }
                redirectAttributes.addFlashAttribute("msg",msg);
                return "redirect:http://order.gulimall.com/toTrade";
            }
        } catch (Exception e) {
            String message = e.getMessage();
            redirectAttributes.addFlashAttribute("msg",message);
        }
        return "redirect:http://order.gulimall.com/toTrade";
    }
}

