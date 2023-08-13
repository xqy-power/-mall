package com.xqy.gulimall.order.vo;

import com.xqy.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * 提交订单回答签证官
 *
 * @author xqy
 * @date 2023/08/08
 */

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order; // 订单信息
    private Integer code; // 0成功 1失败    状态码
}
