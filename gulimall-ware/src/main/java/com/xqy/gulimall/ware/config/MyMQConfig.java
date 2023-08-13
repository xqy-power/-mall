package com.xqy.gulimall.ware.config;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * 我mqconfig
 *
 * @author xqy
 * @date 2023/08/11
 */
@Configuration
public class MyMQConfig {

    // @Bean Binding Queue Exchange



    /**
     * 股票延迟队列
     *
     * @return {@link Queue}
     */
    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 1200000L);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    /**
     * 股票发行队列
     *
     * @return {@link Queue}
     */
    @Bean
    public Queue stockReleaseQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    /**
     * 股票事件交换
     *
     * @return {@link Exchange}
     */
    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }

    /**
     * 订单创建订单绑定
     * 订单创建绑定
     *
     * @return {@link Binding}
     */
    @Bean
    public Binding stockCreateStockBinding() {
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.locked", null);
    }

    /**
     * 订单释放订单绑定
     * 订单发布绑定
     *
     * @return {@link Binding}
     */
    @Bean
    public Binding stockReleaseStockBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.release", null);
    }


}
