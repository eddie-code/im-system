package com.learn.im.common.model.message;

import com.learn.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class MessageReadedContent extends ClientInfo {

    /**
     * 序列号
     */
    private long messageSequence;

    /**
     * 发起
     */
    private String fromId;

    /**
     * 群ID
     */
    private String groupId;

    /**
     * 发送给谁
     */
    private String toId;

    /**
     * 标识会话
     */
    private Integer conversationType;

}