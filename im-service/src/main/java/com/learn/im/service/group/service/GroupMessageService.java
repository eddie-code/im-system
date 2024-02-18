package com.learn.im.service.group.service;

import com.learn.im.codec.pack.message.ChatMessageAck;
import com.learn.im.common.ResponseVO;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.command.GroupEventCommand;
import com.learn.im.common.model.ClientInfo;
import com.learn.im.common.model.GroupChatMessageContent;
import com.learn.im.common.model.MessageContent;
import com.learn.im.common.model.message.OfflineMessageContent;
import com.learn.im.service.group.model.req.SendGroupMessageReq;
import com.learn.im.service.message.model.resp.SendMessageResp;
import com.learn.im.service.message.service.MessageStoreService;
import com.learn.im.service.message.service.CheckSendMessageService;
import com.learn.im.service.seq.RedisSeq;
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
public class GroupMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    RedisSeq redisSeq;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        final AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), r -> {
            Thread thread = new Thread(r);
            // 设置守护线程;
            // 将一个用户线程设置为守护线程的方式是在 线程对象创建 之前 用线程对象的setDaemon方法。
            thread.setDaemon(true);
            thread.setName("message-group-thread-" + num.getAndIncrement());
            return thread;
        });
    }

    public void process(GroupChatMessageContent messageContent) {

        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId(); // 单聊=toId  群里=groupId
        Integer appId = messageContent.getAppId();

        GroupChatMessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(), GroupChatMessageContent.class);
        if (messageFromMessageIdCache != null) {
            threadPoolExecutor.execute(() -> {
                // 1、回ack成功给自己
                ack(messageContent, ResponseVO.successResponse());
                // 2、发送消息给同步在线端
                syncToSender(messageContent, messageContent);
                // 3、发送消息给对方在线端
                dispatchMessage(messageContent);
            });
        }

        // 群聊消息有序性： seq 进行排序 格式：（appId + Seq + groupId）
        long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":" + Constants.SeqConstants.GroupMessage + messageContent.getGroupId());
        messageContent.setMessageSequence(seq);

        //前置校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友
        threadPoolExecutor.execute(() -> {
            // 插入数据
            messageStoreService.storeGroupMessage(messageContent);

            // 离线消息存储
            List<String> groupMemberId = imGroupMemberService.getGroupMemberId(
                    messageContent.getGroupId(),
                    messageContent.getAppId()
            );
            messageContent.setMemberId(groupMemberId);
            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(messageContent,offlineMessageContent);
            offlineMessageContent.setToId(messageContent.getGroupId());
            messageStoreService.storeGroupOfflineMessage(offlineMessageContent,groupMemberId);

            // 1、回ack成功给自己
            ack(messageContent, ResponseVO.successResponse());
            // 2、发送消息给同步在线端
            syncToSender(messageContent, messageContent);
            // 3、发送消息给对方在线端
            dispatchMessage(messageContent);

            // 将 messageId 存入缓存中
            messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(), messageContent);

        });
    }

    /**
     * 回ack成功给自己
     */
    private void ack(MessageContent messageContent, ResponseVO responseVO) {
//        log.info("msg ack,msgId={},checkResut{}", messageContent.getMessageId(), responseVO.getCode());
        // 组装Ack实体
        ChatMessageAck chatMessageAck = new ChatMessageAck(
                messageContent.getMessageId(),
                messageContent.getMessageSequence()
        );
        responseVO.setData(chatMessageAck);
        // 发消息
        messageProducer.sendToUser(
                messageContent.getFromId(),
                GroupEventCommand.GROUP_MSG_ACK,
                responseVO,
                messageContent
        );
    }

    /**
     * 发送消息给同步在线端
     */
    private void syncToSender(GroupChatMessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(
                messageContent.getFromId(),
                GroupEventCommand.MSG_GROUP, // 群聊消息收发 2104
                messageContent,
                messageContent
        );
    }

    /**
     * 发送消息给对方在线端
     */
    private void dispatchMessage(GroupChatMessageContent messageContent) {
        for (String memberId : messageContent.getMemberId()) {
            // 判断这个成员不能是我们的发送方， 在 process方法里面第二点已经发送过了
            if (!memberId.equals(messageContent.getFromId())) {
                messageProducer.sendToUser(
                        memberId,
                        GroupEventCommand.MSG_GROUP,
                        messageContent,
                        messageContent.getAppId()
                );
            }
        }
    }

    /**
     * 前置校验
     */
    private ResponseVO imServerPermissionCheck(String fromId, String toId,Integer appId){
        return checkSendMessageService.checkGroupMessage(fromId, toId,appId);
    }


    public SendMessageResp send(SendGroupMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        GroupChatMessageContent message = new GroupChatMessageContent();
        BeanUtils.copyProperties(req,message);

        messageStoreService.storeGroupMessage(message);

        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());
        //2.发消息给同步在线端
        syncToSender(message,message);
        //3.发消息给对方在线端
        dispatchMessage(message);

        return sendMessageResp;
    }
}