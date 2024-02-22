package com.learn.im.common.model.message;

import lombok.Data;

/**
 * @author lee
 * @description 离线消息
 */
@Data
public class OfflineMessageContent {

    private Integer appId;

    /**
     * messageBodyId
     */
    private Long messageKey;

    /**
     * messageBody
     */
    private String messageBody;

    private Long messageTime;

    private String extra;

    private Integer delFlag;

    private String fromId;

    private String toId;

    /**
     * 序列号
     */
    private Long messageSequence;

    private String messageRandom;

    private Integer conversationType;

    private String conversationId;

}
