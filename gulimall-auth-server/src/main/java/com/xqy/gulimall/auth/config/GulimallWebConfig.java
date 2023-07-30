package com.xqy.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * gulimall网络配置
 *
 * @author xqy
 * @date 2023/07/29
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        @GetMapping("/login.html")
//        public String login() {
//            return "login";
//        }
//        @GetMapping("/reg.html")
//        public String reg() {
//            return "reg";
//        }
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
