package com.learn.im.service.message.service;

import com.learn.im.codec.pack.message.ChatMessageAck;
import com.learn.im.common.ResponseVO;
import com.learn.im.common.enums.command.MessageCommand;
import com.learn.im.common.model.ClientInfo;
import com.learn.im.common.model.MessageContent;
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
public class P2PMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    public void process(MessageContent messageContent) {

        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        //前置校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友
        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
        if (responseVO.isOk()) {
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

}
