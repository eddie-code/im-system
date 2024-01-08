package com.learn.im.common.model.message;

import com.learn.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class MessageReciveAckContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;

}
