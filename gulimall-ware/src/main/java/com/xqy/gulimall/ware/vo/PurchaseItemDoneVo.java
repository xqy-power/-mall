package com.xqy.gulimall.ware.vo;

import lombok.Data;

/**
 * 购买物品做签证官
 *
 * @author xqy
 * @date 2023/01/18
 */
@Data
public class PurchaseItemDoneVo {
//     "itemId":1,
//             "status":4,
//             "reason":""

    private Long itemId;
    private Integer status;
    private String reason;
}
