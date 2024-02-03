package com.learn.im.service.message.service;

import com.learn.im.codec.pack.message.MessageReadedPack;
import com.learn.im.common.enums.command.Command;
import com.learn.im.common.enums.command.GroupEventCommand;
import com.learn.im.common.enums.command.MessageCommand;
import com.learn.im.common.model.message.MessageReadedContent;
import com.learn.im.common.model.message.MessageReciveAckContent;
import com.learn.im.service.conversation.service.ConversationService;
import com.learn.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lee
 * @description
 */
@Service
public class MessageSyncService {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ConversationService conversationService;

    /**
     * 消息接收确认
     */
    public void receiveMark(MessageReciveAckContent messageReciveAckContent) {
        messageProducer.sendToUser(messageReciveAckContent.getToId(),
                MessageCommand.MSG_RECIVE_ACK,
                messageReciveAckContent,
                messageReciveAckContent.getAppId()
        );
    }

    /**
     * 消息已读。更新会话的seq，通知在线的同步端发送指定command ，发送已读回执通知对方（消息发起方）我已读
     */
    public void readMark(MessageReadedContent messageReadedContent) {

        conversationService.messageMarkRead(messageReadedContent);

        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReadedContent, messageReadedPack);
        //消息已读通知给同步端 1053
        syncToSender(messageReadedPack, messageReadedContent, MessageCommand.MSG_READED_NOTIFY);

        // 发送给所有端的方法
        messageProducer.sendToUser(messageReadedContent.getToId(), MessageCommand.MSG_READED_RECEIPT, messageReadedPack, messageReadedContent.getAppId());
    }

    /**
     * 发送给tcp服务的数据包，都写在 im-codec 里面
     */
    private void syncToSender(MessageReadedPack pack, MessageReadedContent messageReadedContent, Command command) {

        MessageReadedPack messageReadedPack = new MessageReadedPack();
//        BeanUtils.copyProperties(messageReadedContent, messageReadedPack);

        //发送给自己的其他端
        messageProducer.sendToUserExceptClient(messageReadedContent.getFromId(), command, pack, messageReadedContent);
    }

    public void groupReadMark(MessageReadedContent messageReaded) {
        conversationService.messageMarkRead(messageReaded);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReaded, messageReadedPack);
        //消息已读通知给同步端 1053
        syncToSender(messageReadedPack, messageReaded, GroupEventCommand.MSG_GROUP_READED_NOTIFY);
        if (!messageReaded.getFromId().equals(messageReaded.getToId())) {
            messageProducer.sendToUser(messageReadedPack.getToId(), GroupEventCommand.MSG_GROUP_READED_RECEIPT, messageReaded, messageReaded.getAppId());
        }
    }

}
