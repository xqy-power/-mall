package com.xqy.gulimall.seckill;

import org.redisson.spring.session.config.EnableRedissonHttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、整合Sentinel
 * 		1、导入依赖spring-cloud-starter-alibaba-sentinel
 * 		2、下载sentinel的控制台
 * 		3、配置sentinel控制台地址信息
 * 		4、在控制台调整参数。【默认所有的流控设置保存在内存中，重启失效】
 * 		2、每一个微服务都导入actuator;并配合management.endpoints,web,exposure,include=*
 * 		3、自定义sentinel的流控返回
 */





@SpringBootApplication
@EnableFeignClients  // 开启远程调用功能
@EnableDiscoveryClient // 开启服务注册发现功能
@EnableScheduling  // 开启定时任务功能
@EnableAsync // 开启异步任务功能
@EnableRedisHttpSession // 开启session共享功能
public class GulimallSeckillApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallSeckillApplication.class, args);
	}

}
