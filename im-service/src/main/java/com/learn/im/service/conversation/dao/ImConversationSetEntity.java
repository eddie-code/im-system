package com.learn.im.service.conversation.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
@TableName("im_conversation_set")
public class ImConversationSetEntity {

    //会话id 0_fromId_toId
    private String conversationId;

    //会话类型
    private Integer conversationType;

    private String fromId;

    private String toId;

    /**
     * 是否禁音
     */
    private int isMute;

    /**
     * 是否置顶
     */
    private int isTop;

    /**
     * 序列号
     */
    private Long sequence;

    /**
     * 已读序列号
     */
    private Long readedSequence;

    private Integer appId;
}
