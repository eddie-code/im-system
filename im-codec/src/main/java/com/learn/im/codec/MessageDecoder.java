package com.learn.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.learn.im.codec.proto.Message;
import com.learn.im.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author lee
 * @description
 */
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        //请求头（指令
        // 版本
        // clientType
        // 消息解析类型
        // appId
        // imei长度
        // bodylen）+ imei号 + 请求体

        if (byteBuf.readableBytes() < 28) {
            return;
        }

        // 获取指令
        int command = byteBuf.readInt();

        // 获取version
        int version = byteBuf.readInt();

        // 获取clientType
        int clientType = byteBuf.readInt();

        // 获取appId
        int appId = byteBuf.readInt();

        // 获取messageType
        int messageType = byteBuf.readInt();

        // 获取imeiLength
        int imeiLength = byteBuf.readInt();

        // 获取bodyLen
        int bodyLen = byteBuf.readInt();

        // 粘包拆包问题 （readableBytes() 小于 bodyLen + imeiLength, 就是数据长度不足了）
        if (byteBuf.readableBytes() < bodyLen + imeiLength) {
            // 重置读索引
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] imeiData = new byte[imeiLength];
        byteBuf.readBytes(imeiData);
        String imei = new String(imeiData);

        byte[] bodyData = new byte[bodyLen];
        byteBuf.readBytes(bodyData);

        // 定义实体去接受, 比使用 byteBuf 方便
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setClientType(clientType);
        messageHeader.setCommand(command);
        messageHeader.setLength(bodyLen);
        messageHeader.setVersion(version);
        messageHeader.setMessageType(messageType);
        messageHeader.setImei(imei);

        Message message = new Message();
        message.setMessageHeader(messageHeader);

        // 通过 messageType 来解析 body
        if (messageType == 0x0) {
            // 解析data数据 0x0:Json,0x1:ProtoBuf,0x2:Xml,默认:0x0
            String body = new String(bodyData);
            JSONObject parse = (JSONObject) JSONObject.parse(body);
            message.setMessagePack(parse);
        }

        // 更新读索引
        byteBuf.markReaderIndex();
        list.add(message);

    }
}
