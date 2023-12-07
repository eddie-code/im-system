package com.learn.im.service.friendship.controller;

import com.learn.im.common.ResponseVO;
import com.learn.im.service.friendship.model.req.*;
import com.learn.im.service.friendship.service.ImFriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/friendship")
public class ImFriendShipController {

    @Autowired
    private ImFriendService imFriendService;

    @RequestMapping("/importFriendShip")
    public ResponseVO importFriendShip(@RequestBody @Validated ImporFriendShipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendService.importFriendShip(req);
    }

    @RequestMapping("/addFriend")
    public ResponseVO addFriend(@RequestBody @Validated AddFriendReq req, Integer appId){
        req.setAppId(appId);
        return imFriendService.addFriend(req);
    }

    @RequestMapping("/updateFriend")
    public ResponseVO updateFriend(@RequestBody @Validated UpdateFriendReq req, Integer appId){
        req.setAppId(appId);
        return imFriendService.updateFriend(req);
    }

    @RequestMapping("/deleteFriend")
    public ResponseVO deleteFriend(@RequestBody @Validated DeleteFriendReq req, Integer appId){
        req.setAppId(appId);
        return imFriendService.deleteFriend(req);
    }

    @RequestMapping("/deleteAllFriend")
    public ResponseVO deleteAllFriend(@RequestBody @Validated DeleteFriendReq req, Integer appId){
        req.setAppId(appId);
        return imFriendService.deleteAllFriend(req);
    }

    @RequestMapping("/getAllFriendShip")
    public ResponseVO getAllFriendShip(@RequestBody @Validated GetAllFriendShipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendService.getAllFriendShip(req);
    }

    @RequestMapping("/getRelation")
    public ResponseVO getRelation(@RequestBody @Validated GetRelationReq req, Integer appId){
        req.setAppId(appId);
        return imFriendService.getRelation(req);
    }

    @RequestMapping("/checkFriend")
    public ResponseVO checkFriend(@RequestBody @Validated CheckFriendShipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendService.checkFriendship(req);
    }

    @RequestMapping("/addBlack")
    public ResponseVO addBlack(@RequestBody @Validated AddFriendShipBlackReq req, Integer appId){
        req.setAppId(appId);
        return imFriendService.addBlack(req);
    }

    @RequestMapping("/deleteBlack")
    public ResponseVO deleteBlack(@RequestBody @Validated DeleteBlackReq req, Integer appId){
        req.setAppId(appId);
        return imFriendService.deleteBlack(req);
    }

    @RequestMapping("/checkBlck")
    public ResponseVO checkBlck(@RequestBody @Validated CheckFriendShipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendService.checkBlck(req);
    }


}
