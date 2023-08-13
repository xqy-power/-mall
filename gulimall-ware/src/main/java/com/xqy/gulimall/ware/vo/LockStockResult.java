package com.xqy.gulimall.ware.vo;

import lombok.Data;

/**
 * 锁结果
 *
 * @author xqy
 * @date 2023/08/09
 */
@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}
