package com.learn.im.service.message.controller;

import com.learn.im.common.ResponseVO;
import com.learn.im.common.model.SyncReq;
import com.learn.im.service.message.model.req.SendMessageReq;
import com.learn.im.service.message.service.MessageSyncService;
import com.learn.im.service.message.service.P2PMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lee
 * @description 提供管理员使用
 */
@RestController
@RequestMapping("v1/message")
public class MessageController {

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    @RequestMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req, Integer appId) {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2PMessageService.send(req));
    }

    /**
     * 内部方法
     */
    @RequestMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated SendMessageReq req) {
        return p2PMessageService.imServerPermissionCheck(
                req.getFromId(), req.getToId(), req.getAppId()
        );
    }

    /**
     * 增量拉取离线消息
     */
    @RequestMapping("/syncOfflineMessage")
    public ResponseVO syncOfflineMessage(@RequestBody @Validated SyncReq req, Integer appId)  {
        req.setAppId(appId);
        return messageSyncService.syncOfflineMessage(req);
    }

}
