package com.learn.im.common.model.message;

import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class ImMessageBody {

    private Integer appId;

    /**
     * messageBodyId
     */
    private Long messageKey;

    /**
     * messageBody
     */
    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;

}
