package com.xqy.gulimall.member.exception;

/**
 * 手机存在异常
 *
 * @author xqy
 * @date 2023/07/29
 */
public class PhoneExistException extends RuntimeException{

        public PhoneExistException() {
            super("手机号已存在");
        }
}
