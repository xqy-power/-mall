package com.xqy.gulimall.cart.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.xqy.common.utils.R;
import com.xqy.gulimall.cart.feign.ProductFeignService;
import com.xqy.gulimall.cart.interceptor.CartInterceptor;
import com.xqy.gulimall.cart.service.CartService;
import com.xqy.gulimall.cart.vo.Cart;
import com.xqy.gulimall.cart.vo.CartItem;
import com.xqy.gulimall.cart.vo.SkuInfoVo;
import com.xqy.gulimall.cart.vo.UserInfoTo;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 车服务impl
 *
 * @author xqy
 * @date 2023/08/01
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    private final String CART_PREFIX = "gulimall:cart:";

    /**
     * 添加到购物车
     *
     * @param skuId sku id
     * @param num   全国矿工工会
     * @return {@link CartItem}
     */
    @Override
    public CartItem addToCart(Long skuId, int num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();


        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            //购物车没有此商品，进行新增商品操作
            //添加新商品到购物车
            CartItem cartItem = new CartItem();
            //开启线程异步
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                //1.远程查询当前要添加的商品的信息
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    //2.将商品添加到购物车
                    cartItem.setCheck(true);
                    cartItem.setSkuId(skuId);
                    cartItem.setTitle(skuInfo.getSkuTitle());
                    cartItem.setImage(skuInfo.getSkuDefaultImg());
                    cartItem.setCount(num);
                    cartItem.setPrice(skuInfo.getPrice());

                }
            }, threadPoolExecutor);

            CompletableFuture<Void> getSkuAttrTask = CompletableFuture.runAsync(() -> {
                //远程调用查询当前Sku的组合信息
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                if (!StringUtils.isEmpty(skuSaleAttrValues)) {
                    cartItem.setSkuAttr(skuSaleAttrValues);
                }
            }, threadPoolExecutor);

            CompletableFuture.allOf(getSkuInfoTask, getSkuAttrTask).get();

            String string = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), string);

            return cartItem;
        } else {
            CartItem cartItem = new CartItem();
            //购物车中 有此商品  只需要修改数量就好
            cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    /**
     * 获取购物车中的某个购物项
     *
     * @param skuId sku id
     * @return {@link CartItem}
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(o, CartItem.class);
    }

    /**
     * 让车
     *
     * @return {@link Cart}
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        Cart cart = new Cart();

        if (userInfoTo.getUserId() != null) {
            //已登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            //判断是否还有临时购物车的数据还没有合并
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (!StringUtils.isEmpty(tempCartItems)) {
                //临时购物车中有数据  进行合并
                for (CartItem item : tempCartItems) {
                    //调用addToCart直接往购物车里添加
                    if (!StringUtils.isEmpty(item.getSkuId()) && !StringUtils.isEmpty(item.count)) {
                        addToCart(item.getSkuId(), item.count);
                    }
                }
                //清除临时购物车的数据
                clearCart(tempCartKey);
            }


            //获取登录后的购物车的数据(包含合并过来的临时购物车的数据和当前用户的购物车的数据)
            List<CartItem> cartItemList = getCartItems(cartKey);
            cart.setItems(cartItemList);
            return cart;

        } else {
            //没有登录
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    /**
     * 获取需要操作的购物车
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //
        String cartKey = "";
        if (!StringUtils.isEmpty(userInfoTo.getUserId())) {
            //用户登录了
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            //用户没有登录，使用临时购物车
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        return stringRedisTemplate.boundHashOps(cartKey);
    }

    private List<CartItem> getCartItems(String cartKey) {

        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && !values.isEmpty()) {
            return values.stream().map((obj) -> {
                String objString = (String) obj;
                return JSON.parseObject(objString, CartItem.class);
            }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 清空购物车数据
     *
     * @param cartKey 车钥匙
     */
    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    /**
     * 检查项目
     *
     * @param skuId sku id
     * @param check 检查
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        if (!StringUtils.isEmpty(cartItem)) {
            cartItem.setCheck(check == 1 ? true : false);
            String string = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), string);
        }
    }

    /**
     * 数项
     *
     * @param skuId sku id
     * @param num   全国矿工工会
     */
    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        if (!StringUtils.isEmpty(cartItem)) {
            cartItem.setCount(num);
            String string = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), string);
        }
    }

    /**
     * 删除项目
     *
     * @param skuId sku id
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    /**
     * 获取用户购物车条目
     *
     * @return {@link List}<{@link CartItem}>
     */
    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            //已登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            //获取登录后的购物车的数据(包含合并过来的临时购物车的数据和当前用户的购物车的数据)
            List<CartItem> cartItems = getCartItems(cartKey);
            List<CartItem> items = null;
            if (cartItems != null) {
                items = cartItems.stream().filter(CartItem::getCheck).map(
                        (item) -> {
                            //更新最新价格
                            BigDecimal r = productFeignService.getPrice(item.getSkuId());
                            item.setPrice(r);
                            return item;
                        }
                ).collect(Collectors.toList());
            }
            return items;
        }
        return null;
    }
}
