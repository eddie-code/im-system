package com.learn.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.learn.im.codec.pack.LoginPack;
import com.learn.im.codec.proto.Message;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.ImConnectStatusEnum;
import com.learn.im.common.enums.command.SystemCommand;
import com.learn.im.common.model.UserSession;
import com.learn.im.tcp.redis.RedisManager;
import com.learn.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;

/**
 * @author lee
 * @description
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {

        // 消息操作指令 十六进制 一个消息的开始通常以0x开头
        Integer command = message.getMessageHeader().getCommand();
        // 登录 command == 0x2328
        if (command == SystemCommand.LOGIN.getCommand()) {

            LoginPack loginPack = JSON.parseObject(JSONObject.toJSONString(message.getMessagePack()),
                    new TypeReference<LoginPack>() {
                    }.getType()
            );

            // 登陸事件
            String userId = loginPack.getUserId();
            channelHandlerContext.channel()
                    .attr(AttributeKey.valueOf("userId"))
                    .set(userId);

            // 将 channel 存储起来

            // Redis map

            UserSession userSession = new UserSession();
            userSession.setAppId(message.getMessageHeader().getAppId());
            userSession.setClientType(message.getMessageHeader().getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            // TODO 使用 Redisson 存到redis
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            // 用户session，appId + UserSessionConstants + 用户id  最终存入Hash格式例子： 10000:userSession:lee
            RMap<String, String> map = redissonClient.getMap(message.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(message.getMessageHeader().getClientType() + "", JSONObject.toJSONString(userSession));

            SessionSocketHolder.put(loginPack.getUserId(), (NioSocketChannel) channelHandlerContext.channel());

        }
    }

}