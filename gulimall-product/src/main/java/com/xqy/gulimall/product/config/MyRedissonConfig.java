package com.xqy.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {
    /**
     * 所有对Redisson的使用都是通过这个 RedissonClient对象
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() throws IOException {
        //创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.133.132:6379");
        //根据config创建出RedissonClient的实例对象
        return Redisson.create(config);
    }

}

