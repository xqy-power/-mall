package com.xqy.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.xqy.gulimall.order.entity.OrderEntity;
import com.xqy.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 订单关闭监听器
 *
 * @author xqy
 * @date 2023/08/12
 */
@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息，准备关闭订单" + entity.getOrderSn());
        try {
            //关闭订单
            orderService.closeOrder(entity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //失败 重新放入队列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
