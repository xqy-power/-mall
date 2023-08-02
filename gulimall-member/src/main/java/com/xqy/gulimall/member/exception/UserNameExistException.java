package com.xqy.gulimall.member.exception;

/**
 * 用户名存在异常
 *
 * @author xqy
 * @date 2023/07/29
 */
public class UserNameExistException extends RuntimeException{

    public UserNameExistException() {
        super("用户名已存在");
    }
}
