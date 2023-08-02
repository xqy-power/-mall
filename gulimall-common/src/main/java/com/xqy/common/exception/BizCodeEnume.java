package com.xqy.common.exception;


/***
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为 5 为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知
 异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 * 10: 通用
 * 001：参数格式校验
 * 002：短信验证码频率太高
 * 11: 商品
 * 12: 订单
 * 13: 购物车
 * 14: 物流
 * 15: 用户
 *
 */

/**
 * 商业代码enume
 * 商业代码异常
 *
 * @author xqy
 * @date 2022/12/17
 */


public enum BizCodeEnume {

    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(111000 , "商品上架异常"),
    SMS_CODE_EXCEPTION(10002,"验证码发送频率过高，请稍后再试"),
    USER_EXIST_EXCEPTION(15001,"用户存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号存在"),
    LOGINACCT_PASSWORD_INVAILD_EXCEPTION(15003,"账号或密码错误");


    private int code;
    private String msg;


    /**
     * 商业代码enume
     *
     * @param code 代码
     * @param msg  错误信息
     */
    BizCodeEnume(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
