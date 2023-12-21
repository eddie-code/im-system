package com.learn.im.service.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
@TableName("im_message_history")
public class ImMessageHistoryEntity {

    private Integer appId;

    private String fromId;

    private String toId;

    /**
     * 写扩散， 故此需要
     */
    private String ownerId;

    /**
     * messageBodyId 消息唯一标识
     */
    private Long messageKey;

    /**
     * 序列号
     */
    private Long sequence;

    /**
     * 随机数： 在客户端发送消息的时候会产生一条随机数
     */
    private String messageRandom;

    /**
     * 客户端发送消息的时间
     */
    private Long messageTime;

    /**
     * 服务端插入的时间
     */
    private Long createTime;

}
