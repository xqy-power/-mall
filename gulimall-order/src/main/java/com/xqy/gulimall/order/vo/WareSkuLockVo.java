package com.xqy.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * 器皿sku锁签证官
 *
 * @author xqy
 * @date 2023/08/09
 */

@Data
public class WareSkuLockVo {

    private String orderSn;  //订单号
    private List<OrderItemVo> locks;  //需要锁住的所有库存信息


}
