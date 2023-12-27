package com.learn.im.common.model.message;

import com.learn.im.common.model.MessageContent;
import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBody messageBody;

}
