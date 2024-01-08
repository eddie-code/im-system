package com.learn.im.codec.pack.message;

import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class MessageReciveServerAckPack {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;

    private Boolean serverSend;
}
