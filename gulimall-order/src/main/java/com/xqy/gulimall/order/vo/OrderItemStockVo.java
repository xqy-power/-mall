package com.xqy.gulimall.order.vo;

import lombok.Data;

/**
 * 订单项股票签证官
 *
 * @author xqy
 * @date 2023/08/08
 */
@Data
public class OrderItemStockVo {

    private Long skuId;
    private Boolean hasStock;
}
