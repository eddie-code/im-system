package com.learn.message.model;

import com.learn.im.common.model.MessageContent;
import com.learn.message.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
