package com.learn.im.service.friendship.model.callback;

import com.learn.im.service.friendship.model.req.FriendDto;
import lombok.Data;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@Data
public class AddFriendAfterCallbackDto {

    private String fromId;

    private FriendDto toItem;
}
