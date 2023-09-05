package com.learn.im.service.friendship.service;

import com.learn.im.common.ResponseVO;
import com.learn.im.service.friendship.model.req.ApproverFriendRequestReq;
import com.learn.im.service.friendship.model.req.FriendDto;
import com.learn.im.service.friendship.model.req.ReadFriendShipRequestReq;


public interface ImFriendShipRequestService {

    public ResponseVO addFienshipRequest(String fromId, FriendDto dto, Integer appId);

    /**
     * 审批好友申请
     *
     * @param req
     * @return
     */
    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req);

    /**
     * 已读所有好友的申请
     *
     * @param req
     * @return
     */
    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);

    public ResponseVO getFriendRequest(String fromId, Integer appId);
}
