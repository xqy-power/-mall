package com.xqy.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * 合并签证官
 *
 * @author xqy
 * @date 2023/01/18
 */
@Data
public class MergeVo {
//    {
//        purchaseId: 1, //整单id
//                items:[1,2,3,4] //合并项集合
//    }
    private Long purchaseId;
    private List<Long> items;

}
