package com.learn.im.service.message.service;

import com.learn.im.codec.pack.message.ChatMessageAck;
import com.learn.im.codec.pack.message.MessageReciveServerAckPack;
import com.learn.im.common.ResponseVO;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.command.MessageCommand;
import com.learn.im.common.model.ClientInfo;
import com.learn.im.common.model.MessageContent;
import com.learn.im.service.message.model.req.SendMessageReq;
import com.learn.im.service.message.model.resp.SendMessageResp;
import com.learn.im.service.seq.RedisSeq;
import com.learn.im.service.utils.ConversationIdGenerate;
import com.learn.im.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lee
 * @description
 */
@Slf4j
@Service
public class P2PMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    RedisSeq redisSeq;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger num = new AtomicInteger();
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                // 设置守护线程;
                // 将一个用户线程设置为守护线程的方式是在 线程对象创建 之前 用线程对象的setDaemon方法。
                thread.setDaemon(true);
                thread.setName("message-process-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    public void process(MessageContent messageContent) {

        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        //前置校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友
//        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
//        if (responseVO.isOk()) {
            threadPoolExecutor.execute(() -> {

                // 客户端可以根据这个 seq 进行排序  格式：（appId + Seq + (from + to) groupId）
                long seq = redisSeq.doGetSeq(
                        messageContent.getAppId() + ":" + Constants.SeqConstants.Message + ":" + ConversationIdGenerate.generateP2PId(messageContent.getFromId(), messageContent.getToId())
                );
                messageContent.setMessageSequence(seq);

                // 插入数据
                messageStoreService.storeP2PMessage(messageContent);
                // 1、回ack成功给自己
                ack(messageContent, ResponseVO.successResponse());
                // 2、发送消息给同步在线端
                syncToSender(messageContent, messageContent);
                // 3、发送消息给对方在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageContent);
                if (clientInfos.isEmpty()) {
                    // 发送接收确认给发送方，要带上是服务端发送的标识
                    reciverAck(messageContent);
                }
            });
//        } else {
//            // 告诉客户端失败了
//            // ack
//            ack(messageContent, responseVO);
//        }
    }

    /**
     * 回ack成功给自己
     */
    private void ack(MessageContent messageContent, ResponseVO responseVO) {
        log.info("msg ack,msgId={},checkResut{}", messageContent.getMessageId(), responseVO.getCode());
        // 组装Ack实体
        ChatMessageAck chatMessageAck = new ChatMessageAck(
                messageContent.getMessageId(),
                messageContent.getMessageSequence()
        );
        responseVO.setData(chatMessageAck);
        // 发消息
        messageProducer.sendToUser(
                messageContent.getFromId(),
                MessageCommand.MSG_ACK,
                responseVO,
                messageContent
        );
    }

    public void reciverAck(MessageContent messageContent) {
        MessageReciveServerAckPack pack = new MessageReciveServerAckPack();
        pack.setFromId(messageContent.getToId());
        pack.setToId(messageContent.getFromId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setMessageSequence(messageContent.getMessageSequence());
        pack.setServerSend(true);
        messageProducer.sendToUser(
                messageContent.getFromId(),
                MessageCommand.MSG_RECIVE_ACK,
                pack,
                new ClientInfo(messageContent.getAppId(), messageContent.getClientType(), messageContent.getImei())
        );
    }

    /**
     * 发送消息给同步在线端
     */
    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(
                messageContent.getFromId(),
                MessageCommand.MSG_P2P, // 单聊消息
                messageContent,
                messageContent
        );
    }

    /**
     * 发送消息给对方在线端
     */
    private List<ClientInfo> dispatchMessage(MessageContent messageContent) {
        List<ClientInfo> clientInfos = messageProducer.sendToUser(
                messageContent.getToId(),
                MessageCommand.MSG_P2P,
                messageContent,
                messageContent.getAppId()
        );
        return clientInfos;
    }

    /**
     * 前置校验
     */
    public ResponseVO imServerPermissionCheck(String fromId, String toId, Integer appId) {
        ResponseVO responseVO = checkSendMessageService.checkSenderFromIdAndMute(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }
        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, appId);
        return responseVO;
    }

    public SendMessageResp send(SendMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        MessageContent messageContent = new MessageContent();
        BeanUtils.copyProperties(req, messageContent);

        //插入数据
        messageStoreService.storeP2PMessage(messageContent);
        sendMessageResp.setMessageKey(messageContent.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        //2.发消息给同步在线端
        syncToSender(messageContent, messageContent);
        //3.发消息给对方在线端
        dispatchMessage(messageContent);

        return sendMessageResp;
    }
}
