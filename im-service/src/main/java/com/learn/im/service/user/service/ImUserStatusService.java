package com.learn.im.service.user.service;

import com.learn.im.service.user.model.UserStatusChangeNotifyContent;
import com.learn.im.service.user.model.req.SubscribeUserOnlineStatusReq;

/**
 * @author lee
 * @description
 */
public interface ImUserStatusService {

    /**
     * 处理用户在线状态通知
     */
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);
}
