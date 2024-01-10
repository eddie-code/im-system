package com.learn.im.common.model.message;

import com.learn.im.common.model.GroupChatMessageContent;
import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBody messageBody;

}
