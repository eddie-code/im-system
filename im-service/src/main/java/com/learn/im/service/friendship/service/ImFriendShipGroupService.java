package com.learn.im.service.friendship.service;

import com.learn.im.common.ResponseVO;
import com.learn.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.learn.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.learn.im.service.friendship.model.req.DeleteFriendShipGroupReq;

/**
 * @author: lee
 * @description:
 **/
public interface ImFriendShipGroupService {

    public ResponseVO addGroup(AddFriendShipGroupReq req);

    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req);

    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);


}
