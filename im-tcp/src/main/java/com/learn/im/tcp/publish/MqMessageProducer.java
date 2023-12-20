package com.learn.im.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.learn.im.codec.proto.Message;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.command.CommandType;
import com.learn.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lee
 * @description
 */
@Slf4j
public class MqMessageProducer {

    /**
     * 发送消息代码
     *
     * @param message
     * @param command
     */
    public static void sendMessage(Message message, Integer command) {
        Channel channel = null;
        String channelName = Constants.RabbitConstants.Im2MessageService;

        // 判断是否群组
        if (command.toString().startsWith("2")) {
            channelName = Constants.RabbitConstants.Im2GroupService;
        }

        try {
            channel = MqFactory.getChannel(channelName);

            JSONObject o = (JSONObject) JSON.toJSON(message.getMessagePack());
            o.put("command", command);
            o.put("clientType", message.getMessageHeader().getClientType());
            o.put("imei", message.getMessageHeader().getImei());
            o.put("appId", message.getMessageHeader().getAppId());
            channel.basicPublish(channelName, "", null, o.toJSONString().getBytes());

        } catch (Exception e) {
            log.error("发送消息出现异常：{}", e.getMessage());
        }
    }
}
