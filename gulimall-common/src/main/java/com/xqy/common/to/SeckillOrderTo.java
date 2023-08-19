package com.xqy.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 秒杀,
 *
 * @author xqy
 * @date 2023/08/16
 */
@Data
public class SeckillOrderTo {
    private String orderSn;  //订单号
    private Long promotionSessionId; //活动场次id
    private Long skuId; //商品id
    private Integer num; //购买数量
    private Long memberId; //会员id
    private BigDecimal seckillPrice; //秒杀价格
}
