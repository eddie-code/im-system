package com.learn.im.service.message.service;

import com.learn.im.codec.pack.message.MessageReadedPack;
import com.learn.im.common.enums.command.Command;
import com.learn.im.common.enums.command.MessageCommand;
import com.learn.im.common.model.message.MessageReadedContent;
import com.learn.im.common.model.message.MessageReciveAckContent;
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
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReadedContent, messageReadedPack);
        syncToSender(messageReadedPack, messageReadedContent);

        // 发送给所有端的方法
        messageProducer.sendToUser(messageReadedContent.getToId(), MessageCommand.MSG_READED_RECEIPT, messageReadedPack, messageReadedContent.getAppId());
    }

    /**
     * 发送给tcp服务的数据包，都写在 im-codec 里面
     */
    private void syncToSender(MessageReadedPack pack, MessageReadedContent messageReadedContent) {

        MessageReadedPack messageReadedPack = new MessageReadedPack();
//        BeanUtils.copyProperties(messageReadedContent, messageReadedPack);

        //发送给自己的其他端
        messageProducer.sendToUserExceptClient(messageReadedContent.getFromId(), MessageCommand.MSG_READED_NOTIFY, pack, messageReadedContent);
    }

}
