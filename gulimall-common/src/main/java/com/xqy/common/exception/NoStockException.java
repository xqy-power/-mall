package com.xqy.common.exception;

import lombok.Getter;

/**
 * 没有股票异常
 *
 * @author xqy
 * @date 2023/08/09
 */

@Getter
public class NoStockException extends RuntimeException{

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    private Long skuId;

    public NoStockException(Long skuId){
        super("商品id:"+skuId+"没有足够的库存了");
    }
    public NoStockException(String msg){
        super(msg);
    }




}
