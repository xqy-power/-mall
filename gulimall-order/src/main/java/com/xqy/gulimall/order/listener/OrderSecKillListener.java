package com.xqy.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.xqy.common.to.SeckillOrderTo;
import com.xqy.gulimall.order.entity.OrderEntity;
import com.xqy.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 以秒杀侦听器
 *
 * @author xqy
 * @date 2023/08/16
 */
@Service
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSecKillListener {

    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Channel channel, Message message) throws IOException {
        System.out.println("收到秒杀订单====》" + seckillOrderTo.getOrderSn());
        try {
            //关闭订单
            orderService.createSecKillOrder(seckillOrderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //失败 重新放入队列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
