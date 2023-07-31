package com.xqy.gulimall.auth.controller;

import com.alibaba.fastjson2.TypeReference;
import com.mysql.cj.log.Log;
import com.xqy.common.constant.AuthServerConstant;
import com.xqy.common.exception.BizCodeEnume;
import com.xqy.common.utils.R;
import com.xqy.common.vo.MemberResponseVo;
import com.xqy.gulimall.auth.feign.MemberFeignService;
import com.xqy.gulimall.auth.feign.ThirdPartFeignService;
import com.xqy.gulimall.auth.vo.UserLoginVo;
import com.xqy.gulimall.auth.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 登录控制器
 *
 * @author xqy
 * @date 2023/07/29
 */
@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 跳转到登录页面
     *
     * @return
     */
    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        //判断是否已经登录
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null) {
            //没登录
            return "login";
        } else {
            //已经登录
            return "redirect:http://gulimall.com";
        }
    }

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        //TODO 接口防刷

        //同一个手机号，60秒内只能发一次
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                //60秒内发送过验证码
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        //验证码的再次校验。redis存key-phone，value-code，过期时间为15分钟 sms:code:122222222 -> 123456
        //生成纯数字的验证码
        Integer uuid = UUID.randomUUID().toString().replaceAll("-", "").hashCode();
        uuid = uuid < 0 ? -uuid : uuid;//String.hashCode() 值会为空

        String code = uuid.toString().substring(0, 5) + "_" + System.currentTimeMillis();

        //redis缓存验证码 防止同一个手机号在60s内再次发送验证码
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 15, TimeUnit.MINUTES);

        thirdPartFeignService.sendCode(phone, code.split("_")[0]);
        return R.ok();
    }

    /**
     * 注册
     * TODO 重定向携带数据，利用session原理，将数据放在session中，
     * 只要跳转到下一个页面，取出数据后，session中的数据就会被删除
     *
     * @param userRegisterVo     用户注册签证官
     * @param result             结果
     * @param redirectAttributes 重定向属性  模拟重定向携带数据
     * @return {@link String}
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo userRegisterVo, BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(
                    FieldError::getField, FieldError::getDefaultMessage));

            redirectAttributes.addFlashAttribute("errors", errors);
            //校验出错，转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //真正的注册，调用远程服务进行注册
        //1.校验验证码
        String code = userRegisterVo.getCode();
        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
        if (!StringUtils.isEmpty(s)) {
            if (code.equals(s.split("_")[0])) {
                //验证码通过，删除验证码
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
                //验证码通过，调用远程服务进行注册
                R r = memberFeignService.register(userRegisterVo);
                if (r.getCode() == 0) {
                    //成功
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    //校验出错，转发到注册页
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                //校验出错，转发到注册页
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            //校验出错，转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }


    }


    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes redirectAttributes, HttpSession session) {
        //feign调用登录
        R loginMember = memberFeignService.login(userLoginVo);
        if (loginMember.getCode() == 0) {
            MemberResponseVo data = loginMember.getData("data", new TypeReference<MemberResponseVo>() {
            });
//            ("登录成功：用户信息：{}", data);
            System.err.println("登录成功：用户信息：==>" + data);
            //登录成功，将登录成功的信息保存到springSession
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            //登录成功，跳转到首页
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", loginMember.getData("msg", new TypeReference<String>() {
            }));
            //登录失败，跳转到登录页
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}



