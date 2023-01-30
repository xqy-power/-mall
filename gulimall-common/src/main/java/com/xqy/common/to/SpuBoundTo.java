package com.xqy.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * spu绑定到
 *
 * @author xqy
 * @date 2023/01/04
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
