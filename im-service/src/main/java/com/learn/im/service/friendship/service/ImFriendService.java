package com.learn.im.service.friendship.service;

import com.learn.im.common.ResponseVO;
import com.learn.im.common.model.RequestBase;
import com.learn.im.service.friendship.model.req.*;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
public interface ImFriendService {

    /**
     * 导入关系链（用于第三方系统）
     *
     * @param req
     * @return
     */
    public ResponseVO importFriendShip(ImporFriendShipReq req);

    /**
     * 添加好友
     *
     * @param req
     * @return
     */
    public ResponseVO addFriend(AddFriendReq req);

    /**
     * 修改好友
     *
     * @param req
     * @return
     */
    public ResponseVO updateFriend(UpdateFriendReq req);

    /**
     * 删除好友
     *
     * @param req
     * @return
     */
    public ResponseVO deleteFriend(DeleteFriendReq req);

    /**
     * 删除全部好友
     *
     * @param req
     * @return
     */
    public ResponseVO deleteAllFriend(DeleteFriendReq req);

    /**
     * 拉取所有的好友
     *
     * @param req
     * @return
     */
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req);

    /**
     * 拉取指定的好友
     *
     * @param req
     * @return
     */
    public ResponseVO getRelation(GetRelationReq req);

    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId);

    /**
     * 检查好友关系
     *
     * @param req
     * @return
     */
    public ResponseVO checkFriendship(CheckFriendShipReq req);

    /**
     * 添加黑名单
     *
     * @param req
     * @return
     */
    public ResponseVO addBlack(AddFriendShipBlackReq req);

    /**
     * 删除黑名单
     *
     * @param req
     * @return
     */
    public ResponseVO deleteBlack(DeleteBlackReq req);

    /**
     * 校验黑名单
     *
     * @param req
     * @return
     */
    public ResponseVO checkBlck(CheckFriendShipReq req);

}
