package com.learn.im.service.message.service;

import com.learn.im.common.enums.command.MessageCommand;
import com.learn.im.common.model.message.MessageReciveAckContent;
import com.learn.im.service.utils.MessageProducer;
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
}
