package com.xqy.gulimall.cart.controller;

import com.xqy.common.constant.AuthServerConstant;
import com.xqy.gulimall.cart.interceptor.CartInterceptor;
import com.xqy.gulimall.cart.service.CartService;
import com.xqy.gulimall.cart.vo.Cart;
import com.xqy.gulimall.cart.vo.CartItem;
import com.xqy.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

/**
 * 车控制器
 *
 * @author xqy
 * @date 2023/08/01
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;


    /**
     * 删除项目
     *
     * @param skuId sku id
     * @return {@link String}
     */
    @GetMapping("deleteItem")
    public String deleteItem(@RequestParam Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 数项
     *
     * @param skuId sku id
     * @param num   全国矿工工会
     * @return {@link String}
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId , @RequestParam("num") Integer num) {
        cartService.countItem(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }


    /**
     * 检查项目
     *
     * @param skuId sku id
     * @param check 检查
     * @return {@link String}
     */
    @GetMapping("checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }



    /**
     * 购物车列表页面
     * 浏览器里有一个cookie  user_kry用于标识用户的身份，一个月后过期
     *
     * @return {@link String}
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        //1.快速得到用户id  和 user_key
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }


    /**
     * 添加到购物车
     *
     * @param skuId              sku id
     * @param num                全国矿工工会
     * @param redirectAttributes 重定向属性
     *                           redirectAttributes.addFlashAttribute() 将数据放在session中可以在页面取出，但是只能取出一次
     *                           redirectAttributes.addAttribute()  将数据放到Url地址后面
     * @return {@link String}
     * @throws ExecutionException   执行异常
     * @throws InterruptedException 中断异常
     */
    @GetMapping("addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") int num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 添加到成功页面
     * 不将添加完购物车后直接返回到成功页面，是为了在页面刷新时多次重复提交商品到购物车。
     * 经过重定向，直接查询Redis在返回成功页面。再次刷新页面时就是查询购物车，不在是往购物车添加商品
     *
     * @param skuId sku id
     * @param model 模型
     * @return {@link String}
     */
    @GetMapping("addToCartSuccess.html")
    public String addToSuccessPage(@RequestParam("skuId") Long skuId,
                                   Model model) {
        //重定向到成功页面，再次查询购物车数据即可
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }
}
