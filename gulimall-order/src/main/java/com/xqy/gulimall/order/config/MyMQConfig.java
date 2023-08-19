package com.xqy.gulimall.order.config;

import com.rabbitmq.client.Channel;
import com.xqy.gulimall.order.entity.OrderEntity;
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
     * 订单延迟队列
     *
     * @return {@link Queue}
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 900000L);
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    /**
     * 订单发布队列
     *
     * @return {@link Queue}
     */
    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.order.queue", true, false, false);
    }


    /**
     * 订单秒杀订单
     *
     * @return {@link Queue}
     */
    @Bean
    public Queue orderSecKillOrder() {
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    /**
     * 事件顺序交换
     *
     * @return {@link Exchange}
     */
    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }

    /**
     * 订单创建订单绑定
     * 订单创建绑定
     *
     * @return {@link Binding}
     */
    @Bean
    public Binding orderCreateOrderBinding() {
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);
    }

    /**
     * 订单释放订单绑定
     * 订单发布绑定
     *
     * @return {@link Binding}
     */
    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order", null);
    }

    /**
     * 订单释放直接和库存释放绑定
     *
     * @return {@link Binding}
     */
    @Bean
    public Binding orderReleaseStockBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.other.#", null);
    }

    /**
     * orderseckill绑定  秒杀队列 同交换机绑定
     *
     * @return {@link Binding}
     */
    @Bean
    public Binding orderseckillBinding() {
        return new Binding("order.seckill.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.seckill.order", null);
    }


}
