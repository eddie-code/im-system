package com.learn.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.learn.im.codec.pack.LoginPack;
import com.learn.im.codec.pack.message.ChatMessageAck;
import com.learn.im.codec.proto.Message;
import com.learn.im.codec.proto.MessagePack;
import com.learn.im.common.ResponseVO;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.ImConnectStatusEnum;
import com.learn.im.common.enums.command.GroupEventCommand;
import com.learn.im.common.enums.command.MessageCommand;
import com.learn.im.common.enums.command.SystemCommand;
import com.learn.im.common.model.UserClientDto;
import com.learn.im.common.model.UserSession;
import com.learn.im.common.model.message.CheckSendMessageReq;
import com.learn.im.tcp.feign.FeignMessageService;
import com.learn.im.tcp.publish.MqMessageProducer;
import com.learn.im.tcp.redis.RedisManager;
import com.learn.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;

/**
 * @author lee
 * @description 处理文本协议数据，处理Message类型的数据，websocket专门处理文本的frame就是Message
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private Integer brokerId;

    private String logicUrl;

    private FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId, String logicUrl) {
        this.brokerId = brokerId;
        feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))//设置超时时间
                .target(FeignMessageService.class, logicUrl);
    }

    /**
     * 读到客户端的内容并且向客户端去写内容
     *
     * @param channelHandlerContext
     * @param message
     * @throws Exception
     */
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
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.Imei)).set(message.getMessageHeader().getImei());

            // 将 channel 存储起来
            UserSession userSession = new UserSession();
            userSession.setAppId(message.getMessageHeader().getAppId());
            userSession.setClientType(message.getMessageHeader().getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setImei(message.getMessageHeader().getImei());

            try {
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            }catch (Exception e){
                e.printStackTrace();
            }

            // Redis map
            // TODO 使用 Redisson 存到redis
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            // 用户session，appId + UserSessionConstants + 用户id  最终存入Hash格式例子： 10000:userSession:lee
            RMap<String, String> map = redissonClient.getMap(message.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(
                    message.getMessageHeader().getClientType() + ":" + message.getMessageHeader().getImei(),
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

            UserClientDto dto = new UserClientDto();
            dto.setImei(message.getMessageHeader().getImei());
            dto.setUserId(loginPack.getUserId());
            dto.setClientType(message.getMessageHeader().getClientType());
            dto.setAppId(message.getMessageHeader().getAppId());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(dto));

        } else if (command == SystemCommand.LOGOUT.getCommand()) { // 用户退出
            // 删除session
            // redis 删除
            SessionSocketHolder.removeUserSession((NioSocketChannel) channelHandlerContext.channel());
        } else if (command == SystemCommand.PING.getCommand()) { // 心跳检测
            // 设置为当前时间
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        }else if(command == MessageCommand.MSG_P2P.getCommand() || command == GroupEventCommand.MSG_GROUP.getCommand()) {

            try {
                String toId = "";
                CheckSendMessageReq req = new CheckSendMessageReq();
                req.setAppId(message.getMessageHeader().getAppId());
                req.setCommand(message.getMessageHeader().getCommand());
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(message.getMessagePack()));
                String fromId = jsonObject.getString("fromId");
                if (command == MessageCommand.MSG_P2P.getCommand()) {
                    toId = jsonObject.getString("toId");
                } else {
                    toId = jsonObject.getString("groupId");
                }
                req.setToId(toId);
                req.setFromId(fromId);

                ResponseVO responseVO = feignMessageService.checkSendMessage(req);
                if (responseVO.isOk()) {
                    MqMessageProducer.sendMessage(message, command);
                } else {
                    Integer ackCommand = 0;
                    if (command == MessageCommand.MSG_P2P.getCommand()) {
                        ackCommand = MessageCommand.MSG_ACK.getCommand();
                    } else {
                        ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                    }

                    ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                    responseVO.setData(chatMessageAck);
                    MessagePack<ResponseVO> ack = new MessagePack<>();
                    ack.setData(responseVO);
                    ack.setCommand(ackCommand);
                    channelHandlerContext.channel().writeAndFlush(ack);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            MqMessageProducer.sendMessage(message,command);
        }

    }

    //表示 channel 处于不活动状态
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //设置离线
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // ... 为了不影响原来业务代码, 新建一个 HeartBeatHandler 去处理心跳检测
    }

}
