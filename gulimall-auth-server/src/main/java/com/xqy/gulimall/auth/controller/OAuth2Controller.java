package com.xqy.gulimall.auth.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.xqy.common.utils.HttpUtils;
import com.xqy.common.utils.R;
import com.xqy.gulimall.auth.feign.MemberFeignService;
import com.xqy.gulimall.auth.vo.GiteeUserVo;
import com.xqy.common.vo.MemberResponseVo;
import com.xqy.gulimall.auth.vo.SociaUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * oauth2控制器
 *
 * @author xqy
 * @date 2023/07/30
 */
@Controller
@Slf4j
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;
    @GetMapping("/oauth2.0/gitee/success")
    public String gitee(@RequestParam String code , HttpSession session ,
                        HttpServletResponse httpServletResponse) throws Exception {
        Map<String, String> header = new HashMap<>();
        Map<String, String> query = new HashMap<>();
        //1、收到授权码，去申请令牌token
        Map<String, String> map = new HashMap<String,String>();
        map.put("grant_type","authorization_code");
        map.put("client_id","dd51577e488754fcf4618226f5dc4ecd4284cfb070881cc5315a7a56dadeeb27");
        map.put("client_secret","39855a4e99eaf9e7fe19adaacc5e9c4c22e0201ce298b8503d7fb582dd9f45c8");
        map.put("redirect_uri","http://auth.gulimall.com/oauth2.0/gitee/success");
        map.put("code",code);
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", header, query, map);

        //处理
        if (response.getStatusLine().getStatusCode() == 200) {
            //获取到了token
            String json = EntityUtils.toString(response.getEntity());
            SociaUser sociaUser = JSON.parseObject(json, SociaUser.class);
            System.err.println("sociaUser = " + sociaUser);

            //获取用户id信息
            //2、携带token，去请求用户信息
            HttpResponse re = HttpUtils.doGet("https://gitee.com", "/api/v5/user?access_token=" + sociaUser.getAccess_token(), "get", header, query);
            GiteeUserVo user = new GiteeUserVo();
            if (re.getStatusLine().getStatusCode() == 200) {
                String json1 = EntityUtils.toString(re.getEntity());
                user = JSON.parseObject(json1, GiteeUserVo.class);
                if (user.getId()!=null){
                    sociaUser.setUid(user.getId());
                }
                if (!StringUtils.isEmpty(user)){
                    sociaUser.setGiteeUserVo(user);
                }
                System.err.println("user = " + user);
            }

            //判断当前用户如果是第一次进入，自动注册（为当前社交用户生成一个会员账号，以后这个社交账号就对应这个会员）
            R r = memberFeignService.oauthLogin(sociaUser);
            if (r.getCode() == 0) {
                //登录成功
                MemberResponseVo data = r.getData("data", new TypeReference<MemberResponseVo>() {
                });
                //保存登录状态 第一次使用session，命令浏览器保存卡号 jsessionid=xxx
                //以后浏览器访问哪个网站就会带上这个jsessionid，直到卡号失效
                //子域之间 gulimall.com auth.gulimall.com order.gulimall.com
                //发卡的时候设置cookie的时候设置为父域名，这样所有子域名都能使用这个cookie
                //TODO 默认发的令牌，session=NTkyOWEyNGMtN2Q3OC00Njk5LWI5OGYtNjE4M2E2  作用域：当前域 （解决子域session共享问题）
                //TODO 使用JSON的序列化方式，将对象序列化为json字符串，保存到redis中
                session.setAttribute("loginUser",data);
//                httpServletResponse.addCookie(new Cookie("JSESSIONID",));


                log.info("登录成功:用户{}",data.toString());
                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }

        } else {
            //获取token失败
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }
}
