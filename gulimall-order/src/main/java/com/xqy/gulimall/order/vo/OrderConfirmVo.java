package com.xqy.gulimall.order.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单确认签证官
 *
 * @author xqy
 * @date 2023/08/07
 */
@Data
public class OrderConfirmVo implements Serializable{

    //收货地址，ums_member_receive_address表
    List<MemberAddressVo> address;
    //所有选中的购物项
    List<OrderItemVo> items;
    //优惠券积分
    private Integer integration;
//    private BigDecimal payPrice; //应付价格
//    private BigDecimal total; //订单总额

    Map<Long,Boolean> stocks; //库存信息

    private String orderToken; //防重令牌

    public Integer getCount(){
        Integer i = 0;
        if(items != null && items.size() > 0){
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        return i;
    }

    /**
     * 得到总额
     *
     * @return {@link BigDecimal}
     */
    public BigDecimal getTotal(){
        BigDecimal sum = new BigDecimal("0");
        if(items != null && items.size() > 0){
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    /**
     * 获得支付价格
     *
     * @return {@link BigDecimal}
     */
    public BigDecimal getPayPrice(){
        return getTotal();
    }

}
