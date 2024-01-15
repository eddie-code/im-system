package com.learn.im.codec.pack.message;

import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class MessageReadedPack {

    /**
     * 序列号
     */
    private long messageSequence;

    /**
     * 发起
     */
    private String fromId;

    /**
     * 发送给谁
     */
    private String toId;

    /**
     * 标识会话
     */
    private Integer conversationType;

}
