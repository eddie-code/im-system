package com.learn.im.service.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
@TableName("im_message_body")
public class ImMessageBodyEntity {

    private Integer appId;

    /**
     * messageBodyId  消息唯一标识
     */
    private Long messageKey;

    /**
     * messageBody 消息数据
     */
    private String messageBody;

    /**
     * 预留字段： 通过该key对消息进行加密解密
     */
    private String securityKey;

    /**
     * 客户端发送消息的时间
     */
    private Long messageTime;

    /**
     * 服务端插入的时间
     */
    private Long createTime;

    /**
     * 拓展
     */
    private String extra;

    /**
     * 删除标识
     */
    private Integer delFlag;

}
