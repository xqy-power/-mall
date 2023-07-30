package com.example.gulimall.thirdparty.controller;

import com.example.gulimall.thirdparty.component.SmsComponent;
import com.xqy.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 短信发送控制器
 *
 * @author xqy
 * @date 2023/07/29
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {


    @Autowired
    SmsComponent smsComponent;
    /**
     * 发送代码
     * 提供给别的服务进行调用
     * @param phone 电话
     * @param code  代码
     * @return {@link R}
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smsComponent.sendSmsCode(phone, code);
        return R.ok();
    }
}
