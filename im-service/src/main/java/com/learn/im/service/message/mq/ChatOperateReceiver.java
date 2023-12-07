package com.learn.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.command.MessageCommand;
import com.learn.im.service.message.service.P2PMessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.xml.ws.handler.MessageContext;
import java.util.Map;

/**
 * @author lee
 * @description 聊天操作消费者
 */
@Slf4j
@Component
public class ChatOperateReceiver {

    @Autowired
    P2PMessageService p2PMessageService;

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
        log.info("CHAT MSG FORM QUEUE ::: {}", msg);

        // mq手动提交的模式, 需要在 catch 里面回复一个ack
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            // 单聊消息 1103
            if (command.equals(MessageCommand.MSG_P2P.getCommand())) {
                MessageContext messageContext = jsonObject.toJavaObject(MessageContext.class);

            }


        } catch (Exception e) {

        }

    }

}
