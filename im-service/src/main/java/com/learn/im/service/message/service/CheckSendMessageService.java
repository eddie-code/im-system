package com.learn.im.service.message.service;

import com.learn.im.common.ResponseVO;
import com.learn.im.common.config.AppConfig;
import com.learn.im.common.enums.*;
import com.learn.im.service.friendship.dao.ImFriendShipEntity;
import com.learn.im.service.friendship.model.req.GetRelationReq;
import com.learn.im.service.friendship.service.ImFriendService;
import com.learn.im.service.group.dao.ImGroupEntity;
import com.learn.im.service.group.model.resp.GetRoleInGroupResp;
import com.learn.im.service.group.service.ImGroupMemberService;
import com.learn.im.service.group.service.ImGroupService;
import com.learn.im.service.user.dao.ImUserDataEntity;
import com.learn.im.service.user.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lee
 * @description 发送消息前置校验
 */
@Service
public class CheckSendMessageService {

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendService imFriendService;

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    AppConfig appConfig;

    /**
     * 校验发送方是否正常
     *
     * @param fromId
     * @param appId
     * @return
     */
    public ResponseVO checkSenderFromIdAndMute(String fromId, Integer appId) {

        // 获取单个用户信息
        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(fromId, appId);
        if (!singleUserInfo.isOk()) {
            return singleUserInfo;
        }

        ImUserDataEntity user = singleUserInfo.getData();
        // 禁用标识(0 未禁用 1 已禁用)
        if (user.getForbiddenFlag() == UserForbiddenFlagEnum.FORBIBBEN.getCode()) {
            // 发送方被禁用
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_FORBIBBEN);
            // 禁言标识
        } else if (user.getSilentFlag() == UserSilentFlagEnum.MUTE.getCode()) {
            // 发送方被禁言
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_MUTE);
        }

        return ResponseVO.successResponse();
    }

    /**
     * 校验是否是好友的关系链
     *
     * @param fromId
     * @param toId
     * @param appId
     * @return
     */
    public ResponseVO checkFriendShip(String fromId,String toId,Integer appId) {

        // 发送消息是否校验关系链
        if (appConfig.isSendMessageCheckFriend()) {
            GetRelationReq fromReq = new GetRelationReq();
            fromReq.setFromId(fromId);
            fromReq.setToId(toId);
            fromReq.setAppId(appId);
            // 查询是否好友关系
            ResponseVO<ImFriendShipEntity> fromRelation = imFriendService.getRelation(fromReq);
            if (!fromRelation.isOk()) {
                return fromRelation;
            }
//            GetRelationReq toReq = new GetRelationReq();
            fromReq.setFromId(toId);
            fromReq.setToId(fromId);
            fromReq.setAppId(appId);
            // 查询是否好友关系
            ResponseVO<ImFriendShipEntity> toRelation = imFriendService.getRelation(fromReq);
            if (!toRelation.isOk()) {
                return toRelation;
            }

            // 好友状态正常 状态 1正常 2删除
            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != fromRelation.getData().getStatus()) {
                // 好友已被删除  (如果我把好友删除了)
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }
            // 好友状态正常 状态 1正常 2删除
            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != toRelation.getData().getStatus()) {
                // 好友已被删除  (如果好友把我删除了)
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            // 发送消息是否校验黑名单
            if (appConfig.isSendMessageCheckBlack()) {
                // 黑色状态正常 0未添加 1正常 2删除
                if (FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode() != fromRelation.getData().getBlack()) {
                    // 好友已被拉黑
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
                }
                // 黑色状态正常 0未添加 1正常 2删除
                if (FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode() != toRelation.getData().getBlack()) {
                    // 对方把你拉黑
                    return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_BLACK_YOU);
                }
            }
        }

        return ResponseVO.successResponse();
    }

    /**
     * 发送群聊消息前置校验
     *
     * @param fromId
     * @param groupId
     * @param appId
     * @return
     */
    public ResponseVO checkGroupMessage(String fromId, String groupId, Integer appId) {

        ResponseVO responseVO = checkSenderFromIdAndMute(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }

        //判断群逻辑
        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(groupId, appId);
        if (!group.isOk()) {
            return group;
        }

        //判断群成员是否在群内
        ResponseVO<GetRoleInGroupResp> roleInGroupOne = imGroupMemberService.getRoleInGroupOne(groupId, fromId, appId);
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }
        GetRoleInGroupResp data = roleInGroupOne.getData();

        //判断群是否被禁言
        //如果禁言 只有裙管理和群主可以发言
        ImGroupEntity groupData = group.getData();
        // 是否全员禁言，0 不禁言；1 全员禁言
        if (groupData.getMute() == GroupMuteTypeEnum.MUTE.getCode() && (data.getRole() == GroupMemberRoleEnum.MAMAGER.getCode() || data.getRole() == GroupMemberRoleEnum.OWNER.getCode())) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_GROUP_IS_MUTE); // 该群禁止发言
        }

        // 禁言时间大于当前时间
        if (data.getSpeakDate() != null && data.getSpeakDate() > System.currentTimeMillis()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_IS_SPEAK); // 群成员被禁言
        }

        return ResponseVO.successResponse();
    }

}
