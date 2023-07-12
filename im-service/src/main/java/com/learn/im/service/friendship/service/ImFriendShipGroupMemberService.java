package com.learn.im.service.friendship.service;

import com.learn.im.common.ResponseVO;
import com.learn.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.learn.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;

/**
 * @author: lee
 * @description:
 **/
public interface ImFriendShipGroupMemberService {

    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    public ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req);

    public int doAddGroupMember(Long groupId, String toId);

    public int clearGroupMember(Long groupId);
}
