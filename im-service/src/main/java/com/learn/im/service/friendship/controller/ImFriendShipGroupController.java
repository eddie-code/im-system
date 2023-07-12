package com.learn.im.service.friendship.controller;

import com.learn.im.common.ResponseVO;
import com.learn.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.learn.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.learn.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;
import com.learn.im.service.friendship.model.req.DeleteFriendShipGroupReq;
import com.learn.im.service.friendship.service.ImFriendShipGroupMemberService;
import com.learn.im.service.friendship.service.ImFriendShipGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: lee
 * @description:
 **/
@RestController
@RequestMapping("v1/friendship/group")
public class ImFriendShipGroupController {

    @Autowired
    private ImFriendShipGroupService imFriendShipGroupService;

    @Autowired
    private ImFriendShipGroupMemberService imFriendShipGroupMemberService;


    @RequestMapping("/add")
    public ResponseVO add(@RequestBody @Validated AddFriendShipGroupReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupService.addGroup(req);
    }

    @RequestMapping("/del")
    public ResponseVO del(@RequestBody @Validated DeleteFriendShipGroupReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupService.deleteGroup(req);
    }

    @RequestMapping("/member/add")
    public ResponseVO memberAdd(@RequestBody @Validated AddFriendShipGroupMemberReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupMemberService.addGroupMember(req);
    }

    @RequestMapping("/member/del")
    public ResponseVO memberdel(@RequestBody @Validated DeleteFriendShipGroupMemberReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupMemberService.delGroupMember(req);
    }


}
