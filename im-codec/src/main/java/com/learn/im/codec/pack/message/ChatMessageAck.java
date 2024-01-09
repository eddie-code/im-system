package com.learn.im.codec.pack.message;

import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class ChatMessageAck {

    private String messageId;

    /**
     * 序列号
     */
    private Long messageSequence;

    public ChatMessageAck(String messageId) {
        this.messageId = messageId;
    }

    public ChatMessageAck(String messageId, Long messageSequence) {
        this.messageId = messageId;
        this.messageSequence = messageSequence;
    }

}
