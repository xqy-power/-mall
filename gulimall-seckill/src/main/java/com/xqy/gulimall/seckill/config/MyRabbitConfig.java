package com.xqy.gulimall.seckill.config;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 我兔子配置
 *
 * @author xqy
 * @date 2023/08/06
 */
@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;



    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        //设置消息确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 确认
             *
             * @param correlationData 关联数据
             * @param b               b
             * @param s               年代
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                //服务器收到消息就会回调
                System.out.println("ConfirmCallback: " + correlationData + " " + b + " " + s);
            }
        });
        this.rabbitTemplate = rabbitTemplate;
    }

    @Autowired
    public void setReturnCallback(RabbitTemplate rabbitTemplate) {
        //设置消息失败回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 返回
             *只要消息没有投递给指定的队列，就触发这个失败回调
             * @param message    消息
             * @param i          i
             * @param s          年代
             * @param s1         年代
             * @param s2         年代
             */
            @Override
            public void returnedMessage(org.springframework.amqp.core.Message message, int i, String s, String s1, String s2) {
                //消息没有投递给指定的队列，就触发这个失败回调
                System.out.println("ReturnCallback: " + message + " " + i + " " + s + " " + s1 + " " + s2);
            }
        });
        this.rabbitTemplate = rabbitTemplate;
    }
}
