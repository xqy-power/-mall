package com.xqy.gulimall.order.to;

import com.xqy.gulimall.order.entity.OrderEntity;
import com.xqy.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建
 *
 * @author xqy
 * @date 2023/08/09
 */

@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;  //订单的应付价格，防止订单价格被篡改

    private BigDecimal fare;  //运费
}
