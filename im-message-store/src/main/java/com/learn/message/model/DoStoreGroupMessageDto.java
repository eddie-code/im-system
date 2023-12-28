package com.learn.message.model;

import com.learn.im.common.model.GroupChatMessageContent;
import com.learn.message.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
