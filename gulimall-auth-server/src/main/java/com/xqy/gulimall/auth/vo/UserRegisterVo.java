package com.xqy.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * 用户注册签证官
 *
 *
 * @author xqy
 * @date 2023/07/29
 */
@Data
public class UserRegisterVo {

    @Length(min = 6,max = 18,message = "用户名长度必须是6-18位")
    @NotEmpty(message = "用户名必须提交")
    private String userName;

    @NotEmpty(message = "密码必须提交")
    @Length(min = 6,max = 18,message = "密码长度必须是6-18位")
    private String password;

    @NotEmpty(message = "手机号码必须提交")
    @Pattern(regexp = "^1[3|4|5|7|8][0-9]\\d{8}$",message = "手机号码格式不正确")
    private String phone;

    @NotEmpty(message = "验证码必须提交")
    private String code;
}
