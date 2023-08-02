package com.xqy.gulimall.cart.vo;

import lombok.Data;

/**
 * 用户信息签证官
 *
 * @author xqy
 * @date 2023/08/01
 */
@Data
public class UserInfoTo {
    private String userId;
    private String userKey;
    private boolean tempUser = false;  //判断是否有临时用户
}
