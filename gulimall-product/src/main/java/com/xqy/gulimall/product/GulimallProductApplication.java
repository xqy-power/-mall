package com.xqy.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 * gulimall产品应用
 *
 * @author xqy
 * @date 2023/01/04
 */
@EnableCaching
@SpringBootApplication
@MapperScan("com.xqy.gulimall.product.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xqy.gulimall.product.feign")
@EnableRedisHttpSession
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
