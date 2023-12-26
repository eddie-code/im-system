package com.learn.im.common.model.message;

import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class CheckSendMessageReq {

    private String fromId;

    private String toId;

    private Integer appId;

    private Integer command;

}