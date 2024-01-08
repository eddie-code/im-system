package com.learn.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.command.MessageCommand;
import com.learn.im.common.model.MessageContent;
import com.learn.im.common.model.message.MessageReciveAckContent;
import com.learn.im.service.message.service.MessageSyncService;
import com.learn.im.service.message.service.P2PMessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author lee
 * @description 聊天操作接收器
 */
@Slf4j
@Component
public class ChatOperateReceiver {

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.Im2MessageService, durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitConstants.Im2MessageService, durable = "true")
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws Exception {
        String msg = new String(message.getBody(), "utf-8");
        log.info("聊天消息表单队列 ::: {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        // mq手动提交的模式, 需要在 catch 里面回复一个ack
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            // 单聊消息 1103
            if (command.equals(MessageCommand.MSG_P2P.getCommand())) {
                // 处理消息
                MessageContent messageContent = jsonObject.toJavaObject(MessageContent.class);
                p2PMessageService.process(messageContent);
            } else if (command.equals(MessageCommand.MSG_RECIVE_ACK.getCommand())) {
                // 消息接收确认
                MessageReciveAckContent messageContent = jsonObject.toJavaObject(MessageReciveAckContent.class);
                messageSyncService.receiveMark(messageContent);
            }
            // 消费成功调用channel.basicAck()
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理消息出现异常：{}", e.getMessage());
            log.error("RMQ_CHAT_TRAN_ERROR", e);
            log.error("NACK_MSG:{}", msg);

            // tips: 当消费失败后，不能使用channel.basicNack() ，应该配合死信队列和存储中间件进行重试
            // 消费失败时调用channel.basicNack()告诉MQ
            // 第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }

    }

}
