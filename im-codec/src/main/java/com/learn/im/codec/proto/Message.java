package com.learn.im.codec.proto;

import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class Message {

    /**
     * 消息头
     */
    private MessageHeader messageHeader;

    /**
     * 消息包
     */
    private Object messagePack;

    @Override
    public String toString() {
        return "Message{" +
                "messageHeader=" + messageHeader +
                ", messagePack=" + messagePack +
                '}';
    }

}
