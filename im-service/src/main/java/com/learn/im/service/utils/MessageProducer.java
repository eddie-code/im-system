package com.learn.im.service.utils;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.learn.im.codec.proto.MessagePack;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.command.Command;
import com.learn.im.common.model.ClientInfo;
import com.learn.im.common.model.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author lee
 * @description
 */
@Slf4j
@Service
public class MessageProducer {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    UserSessionUtils userSessionUtils;

    private String queueName = Constants.RabbitConstants.MessageService2Im;

    public boolean sendMessage(UserSession session, Object msg) {
        try {
            log.info("send message == " + msg);
            // BrokerId 区别那个服务器id
            rabbitTemplate.convertAndSend(queueName, session.getBrokerId() + "", msg);
            return true;
        } catch (Exception e) {
            log.error("send error :" + e.getMessage());
            return false;
        }
    }

    /**
     * 包装数据，调用sendMessage
     */
    public boolean sendPack(String toId, Command command, Object msg, UserSession session) {
        // 路由属性（发送给谁）
        MessagePack messagePack = new MessagePack();
        messagePack.setCommand(command.getCommand());
        messagePack.setToId(toId);
        messagePack.setClientType(session.getClientType());
        messagePack.setAppId(session.getAppId());
        messagePack.setImei(session.getImei());
        // 真正需要发送客户端的数据
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(msg));
        messagePack.setData(jsonObject);
        // 推到MQ
        String body = JSONObject.toJSONString(messagePack);
        return sendMessage(session, body);
    }

    /**
     * 发送给所有端的方法
     */
    public List<ClientInfo> sendToUser(String toId, Command command, Object data, Integer appId) {
        // redis获取用户
        List<UserSession> userSession = userSessionUtils.getUserSession(appId, toId);
        List<ClientInfo> list = new ArrayList<>();
        for (UserSession session : userSession) {
            boolean b = sendPack(toId, command, data, session);
            // 发送成功
            if (b) {
                list.add(new ClientInfo(session.getAppId(), session.getClientType(), session.getImei()));
            }
        }
        // 若返回是空, 证明另外一端没有在线
        return list;
    }

    public void sendToUser(String toId, Integer clientType, String imei, Command command, Object data, Integer appId) {
        // 判断是否后台管理调用, 因为后台管理调用是没有 imei 号
        if (clientType != null && StringUtils.isNotBlank(imei)) {
            // 指定端
            ClientInfo clientInfo = new ClientInfo(appId, clientType, imei);
            sendToUserExceptClient(toId, command, data, clientInfo);
        } else {
            // 发送所有的端
            sendToUser(toId, command, data, appId);
        }
    }

    /**
     * 发送给某个用户的指定客户端
     */
    public void sendToUser(String toId, Command command, Object data, ClientInfo clientInfo) {
        UserSession userSession = userSessionUtils.getUserSession(clientInfo.getAppId(), toId, clientInfo.getClientType(), clientInfo.getImei());
        sendPack(toId, command, data, userSession);
    }

    private boolean isMatch(UserSession sessionDto, ClientInfo clientInfo) {
        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId())
                && Objects.equals(sessionDto.getImei(), clientInfo.getImei())
                && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
    }

    /**
     * 发送给除了某一端的其他端
     */
    public void sendToUserExceptClient(String toId, Command command, Object data, ClientInfo clientInfo) {
        List<UserSession> userSession = userSessionUtils.getUserSession(clientInfo.getAppId(), toId);
        for (UserSession session : userSession) {
            if (!isMatch(session, clientInfo)) {
                sendPack(toId, command, data, session);
            }
        }
    }

}
