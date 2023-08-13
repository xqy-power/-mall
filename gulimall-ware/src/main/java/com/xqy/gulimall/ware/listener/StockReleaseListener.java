package com.xqy.gulimall.ware.listener;


import com.rabbitmq.client.Channel;
import com.xqy.common.to.mq.OrderTo;
import com.xqy.common.to.mq.StockLockedTo;
import com.xqy.gulimall.ware.service.WareSkuService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 股票发行侦听器
 *
 * @author xqy
 * @date 2023/08/11
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息。。。。");
        try {
            wareSkuService.handleStockLockedRelease(stockLockedTo);
            //成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //失败
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitHandler
    public void handOrderCloseRelease(OrderTo orderTo , Message message, Channel channel) throws IOException {
        System.out.println("订单关闭准备解锁库存。。。。");
        try {
            wareSkuService.handleStockLockedRelease(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
