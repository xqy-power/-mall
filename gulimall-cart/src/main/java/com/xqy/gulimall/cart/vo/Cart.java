package com.xqy.gulimall.cart.vo;

import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 *
 * @author xqy
 * @date 2023/07/31
 */
@Data
public class Cart {
    List<CartItem> items;
    private Integer countNum;  //商品数量
    private Integer countType;  //商品类型数量
    private BigDecimal totalAmount; //商品总价
    private BigDecimal reduce = new BigDecimal("0.00");  //减免价格
//    private BigDecimal totalAmountReal; //减免后的商品总价

    public Integer getCountNum() {
        int count = 0;
        if (items != null && !items.isEmpty()) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if (items != null && !items.isEmpty()) {
            for (CartItem item : items) {
                count += 1;
            }
        }
        return count;
    }


    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if (items != null && !items.isEmpty()) {
            for (CartItem item : items) {
                if (item.getCheck()) {
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }

        //减去优惠的价格
        return amount.subtract(getReduce());
    }
}
