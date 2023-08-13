package com.xqy.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 机票签证官
 *
 * @author xqy
 * @date 2023/08/08
 */

@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
