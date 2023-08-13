package com.xqy.gulimall.cart.service;

import com.xqy.gulimall.cart.vo.Cart;
import com.xqy.gulimall.cart.vo.CartItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 车服务
 *
 * @author xqy
 * @date 2023/08/01
 */
public interface CartService {
    CartItem addToCart(Long skuId, int num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车数据
     *
     * @param cartKey 车钥匙
     */
    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
