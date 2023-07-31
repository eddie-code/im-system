package com.learn.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.learn.im.codec.pack.LoginPack;
import com.learn.im.codec.proto.Message;
import com.learn.im.common.enums.command.SystemCommand;
import com.learn.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

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
            SessionSocketHolder.put(loginPack.getUserId(), (NioSocketChannel) channelHandlerContext.channel());

        }
    }

}
