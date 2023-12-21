package com.learn.im.service.group.service;

import com.learn.im.codec.pack.message.ChatMessageAck;
import com.learn.im.common.ResponseVO;
import com.learn.im.common.enums.command.GroupEventCommand;
import com.learn.im.common.model.ClientInfo;
import com.learn.im.common.model.GroupChatMessageContent;
import com.learn.im.common.model.MessageContent;
import com.learn.im.service.message.mq.MessageStoreService;
import com.learn.im.service.message.service.CheckSendMessageService;
import com.learn.im.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void process(GroupChatMessageContent messageContent) {

        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId(); // 单聊=toId  群里=groupId
        Integer appId = messageContent.getAppId();
        //前置校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友
        ResponseVO responseVO = imServerPermissionCheck(fromId, groupId, appId);
        if (responseVO.isOk()) {
            // 插入数据
            messageStoreService.storeGroupMessage(messageContent);
            // 1、回ack成功给自己
            ack(messageContent, ResponseVO.successResponse());
            // 2、发送消息给同步在线端
            syncToSender(messageContent, messageContent);
            // 3、发送消息给对方在线端
            dispatchMessage(messageContent);
        } else {
            // 告诉客户端失败了
            // ack
            ack(messageContent, responseVO);
        }
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
                GroupEventCommand.MSG_GROUP,
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
        // sql :: and role != 3
        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(
                messageContent.getGroupId(),
                messageContent.getAppId()
        );
        for (String memberId : groupMemberId) {
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


}