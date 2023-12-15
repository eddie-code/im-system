package com.learn.im.tcp.utils;

import com.learn.im.codec.config.BootstrapConfig;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author lee
 * @description MQ工厂
 */
public class MqFactory {

    private static ConnectionFactory factory = null;

    private static Channel defaultChannel;

    private static final ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    private static Connection getConnection() throws IOException, TimeoutException {
        return factory.newConnection();
    }

    /**
     * channelName来区分不同的用户的command, 用户组的command
     */
    public static Channel getChannel(String channelName) throws IOException, TimeoutException {
        // 获取channelName
        Channel channel = channelMap.get(channelName);
        // 没有就创建
        if (channel == null) {
            channel = getConnection().createChannel();
            channelMap.put(channelName, channel);
        }
//        System.out.println("channel: " + channel);
        return channel;
    }

    /**
     * 初始化
     */
    public static void init(BootstrapConfig.Rabbitmq rabbitmq) {
        if (factory == null) {
            factory = new ConnectionFactory();
            factory.setHost(rabbitmq.getHost());
            factory.setPort(rabbitmq.getPort());
            factory.setUsername(rabbitmq.getUserName());
            factory.setPassword(rabbitmq.getPassword());
            factory.setVirtualHost(rabbitmq.getVirtualHost());
        }
    }
}
