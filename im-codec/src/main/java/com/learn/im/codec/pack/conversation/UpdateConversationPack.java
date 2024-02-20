package com.learn.im.codec.pack.conversation;

import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class UpdateConversationPack {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private Integer conversationType;

    private Long sequence;

}
