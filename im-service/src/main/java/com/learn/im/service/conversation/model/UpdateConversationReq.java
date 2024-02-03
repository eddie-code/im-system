package com.learn.im.service.conversation.model;

import com.learn.im.common.model.RequestBase;
import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class UpdateConversationReq extends RequestBase {

    private String conversationId;

    /**
     * 是否禁言  1=是
     */
    private Integer isMute;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     *
     */
    private String fromId;

}
