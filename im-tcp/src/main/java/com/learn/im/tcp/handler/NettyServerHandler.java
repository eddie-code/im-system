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
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(loginPack.getUserId());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(message.getMessageHeader().getAppId());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.ClientType)).set(message.getMessageHeader().getClientType());

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
            map.put(
                    String.valueOf(message.getMessageHeader().getClientType()),
                    JSONObject.toJSONString(userSession)
            );
            // channel 信息保存系session里面
            SessionSocketHolder.put(
                    message.getMessageHeader().getAppId(),
                    loginPack.getUserId(),
                    message.getMessageHeader().getClientType(),
                    message.getMessageHeader().getImei(),
                    (NioSocketChannel) channelHandlerContext.channel()
            );
        } else if (command == SystemCommand.LOGOUT.getCommand()) { // 用户退出
            // TODO
            // 删除 session 里面的 channel 信息（既是：客户端传入来的 messageHeader 消息）
            String userId = (String) channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.UserId)).get();
            Integer appId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.AppId)).get();
            Integer clientType = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.ClientType)).get();
            SessionSocketHolder.remove(appId, userId, clientType);

            // 删除 redis 里面的路由关系
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<Object, Object> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
            map.remove(clientType);

            // 关闭路由
            channelHandlerContext.channel().close();
        }
    }

}
