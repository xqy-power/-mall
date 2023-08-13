package com.xqy.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单项签证官
 *
 * @author xqy
 * @date 2023/08/07
 */
@Data
public class OrderItemVo {

    private Long skuId;
    private String title;
    private String image;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    private List<String> skuAttr;

    private BigDecimal weight; //重量
}
