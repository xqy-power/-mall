package com.xqy.gulimall.order.config;

import com.xqy.common.utils.R;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 古丽假装配置
 *
 * @author xqy
 * @date 2023/08/08
 */
@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
//                System.out.println("feign远程之前先执行这里，可以在这里面修改请求头");
                //1.获取到刚进来的请求
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null){
                    HttpServletRequest request = requestAttributes.getRequest();
                    //同步请求头数据
                    String cookie = request.getHeader("Cookie");
                    //给请求头中添加cookie
                    template.header("Cookie",cookie);
                }
            }
        } ;
    }
}
