package com.learn.im.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import com.learn.im.codec.proto.MessagePack;
import com.learn.im.common.constant.Constants;
import com.learn.im.tcp.reciver.process.BaseProcess;
import com.learn.im.tcp.reciver.process.ProcessFactory;
import com.learn.im.tcp.utils.MqFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author lee
 * @description 消息接收器
 */
@Slf4j
public class MessageReciver {

    private static String brokerId;

    private static void startReciverMessage() {

        try {
            // 获取MQ的通道 (Exchanges)
            Channel channel = MqFactory.getChannel(Constants.RabbitConstants.MessageService2Im + brokerId);

            // 队列声明
            channel.queueDeclare(
                    Constants.RabbitConstants.MessageService2Im + brokerId, // Queue
                    true, // 是否持久化
                    false, // 一般 false
                    false, // 是否删除
                    null  // 是否有额外参数
            );

            // 绑定交换机 （暂时没有routingKey, 后面补充）
            channel.queueBind(
                    Constants.RabbitConstants.MessageService2Im + brokerId,  // Queue
                    Constants.RabbitConstants.MessageService2Im,  // Exchanges
                    brokerId // routingKey
            );

            // 消费
            channel.basicConsume(
                    Constants.RabbitConstants.MessageService2Im + brokerId,
                    false, // 是否自动提交, 一般设置 false
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            // TODO 处理消息服务, 发来的消息
                            try {
                                String msgStr = new String(body);
                                // 参考教程是没有提示到需要创建Exchanges=(messageService2Pipeline), 可能这个原因导致打印不出来
                                log.info("消费: {}",msgStr);
                                MessagePack messagePack = JSONObject.parseObject(msgStr, MessagePack.class);
                                BaseProcess messageProcess = ProcessFactory.getMessageProcess(messagePack.getCommand());
                                messageProcess.process(messagePack);

                                channel.basicAck(envelope.getDeliveryTag(),false);

                            }catch (Exception e){
                                e.printStackTrace();
                                channel.basicNack(envelope.getDeliveryTag(),false,false);
                            }
                        }
                    }
            );

        } catch (Exception e) {

        }
    }

    public static void init() {
        startReciverMessage();
    }

    public static void init(String brokerId) {
        if (StringUtils.isBlank(MessageReciver.brokerId)) {
            MessageReciver.brokerId = brokerId;
        }
        startReciverMessage();
    }
}
