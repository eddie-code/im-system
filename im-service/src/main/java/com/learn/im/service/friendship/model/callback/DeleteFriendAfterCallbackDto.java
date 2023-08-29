package com.learn.im.service.friendship.model.callback;

import lombok.Data;

/**
 * @author lee
 * @description
 */
@Data
public class DeleteFriendAfterCallbackDto {

    private String fromId;

    private String toId;
}
