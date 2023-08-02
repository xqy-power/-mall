package com.xqy.gulimall.auth.controller;

import com.xqy.common.constant.AuthServerConstant;
import org.apache.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 注销控制器
 *
 * @author xqy
 * @date 2023/07/31
 */
@Controller
public class LogoutController {
    @GetMapping("/logout.html")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        if (session != null) {
            session.removeAttribute(AuthServerConstant.LOGIN_USER);
            session.invalidate();
            Cookie cookie = new Cookie("GULISESSION", "");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        return "redirect:http://auth.gulimall.com/login.html";
    }
}
