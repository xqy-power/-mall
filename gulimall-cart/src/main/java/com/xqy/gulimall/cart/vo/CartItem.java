package com.xqy.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


/**
 * 购物项
 *
 * @author xqy
 * @date 2023/07/31
 */
@Data
public class CartItem {
    public Long skuId;
    public Boolean check = true;
    public String title;
    public String image;
    public BigDecimal price;
    public Integer count;
    public BigDecimal totalPrice;
    public List<String> skuAttr;

    /**
     * 得到总价格
     *
     * @return {@link BigDecimal}
     */
    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(""+this.count));
    }

}
