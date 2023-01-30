package com.xqy.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * sku减少
 *
 * @author xqy
 * @date 2023/01/04
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private BigDecimal priceStatus;
    private List<MemberPrice> memberPrice;
}
