package com.xqy.gulimall.seckill.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xqy
 * @date 2023/08/19
 */
@Configuration
public class RabbitMessageConverterConfig {

    /**
     * json消息转换器
     *
     * @return {@link MessageConverter}
     */
    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
