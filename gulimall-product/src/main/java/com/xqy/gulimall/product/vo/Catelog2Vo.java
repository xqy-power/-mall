package com.xqy.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author xqy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog2Vo {
    private String catalog1Id; //一级父分类id
    private List<Catelog3Vo> catalog3List;  //三级分类
    private String id;  //二级分类id
    private String name;

    /**
     * 三级分类vo
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catelog3Vo {
        private String catalog2Id; //二级分类id
        private String id; //三级分类id
        private String name;
    }
}
