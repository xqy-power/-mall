package com.xqy.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * 股票锁定
 *
 * @author xqy
 * @date 2023/08/11
 */
@Data
public class StockLockedTo {
    private Long id;  //库存工作单id
    private StockDetailTo detail; //工作单详情

}
