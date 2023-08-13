package com.xqy.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单提交签证官
 *
 * @author xqy
 * @date 2023/08/08
 */

@Data
public class OrderSubmitVo {
    private Long addrId; // 收货地址id
    private Integer payType; // 支付方式
    // 无需提交购物车的数据，去购物车在获取一次即可
    private String orderToken; // 订单令牌
    private BigDecimal payPrice; // 应付价格 验价
    //用户信息，直接去session取出登录的用户即可
    private String note; // 订单备注
}
