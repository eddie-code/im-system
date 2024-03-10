package com.learn.im.service.user.service.impl;

import com.learn.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.command.UserEventCommand;
import com.learn.im.common.model.ClientInfo;
import com.learn.im.common.model.UserSession;
import com.learn.im.service.friendship.service.ImFriendService;
import com.learn.im.service.user.model.UserStatusChangeNotifyContent;
import com.learn.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.learn.im.service.user.service.ImUserStatusService;
import com.learn.im.service.utils.MessageProducer;
import com.learn.im.service.utils.UserSessionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @author lee
 * @description
 */
@Service
public class ImUserStatusServiceImpl implements ImUserStatusService {

    @Autowired
    UserSessionUtils userSessionUtils;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImFriendService imFriendService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content) {

        // 获取所有的UserSession
        List<UserSession> userSession = userSessionUtils.getUserSession(content.getAppId(), content.getUserId());
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        BeanUtils.copyProperties(content, userStatusChangeNotifyPack);
        userStatusChangeNotifyPack.setClient(userSession);

        // 发送给自己的同步端
        syncSender(
                userStatusChangeNotifyPack,
                content.getUserId(),
                content
        );

        // 同步给好友和订阅了自己的人
        dispatcher(
                userStatusChangeNotifyPack,
                content.getUserId(),
                content.getAppId()
        );
    }

    /**
     * 登录人订阅谁， 就推送给谁
     *
     * @param req
     */
    @Override
    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req) {
        Long subExpireTime = 0L;
        if (req != null && req.getSubTime() > 0) {
            subExpireTime = System.currentTimeMillis() + req.getSubTime();
        }

        for (String beSubUserId : req.getSubUserId()) {
            String userKey = req.getAppId() + ":" + Constants.RedisConstants.subscribe + ":" + beSubUserId;
            // 键： Operater 就是 lld...    值： 过期时间
            stringRedisTemplate.opsForHash().put(userKey, req.getOperater(), subExpireTime.toString());
        }
    }

    private void syncSender(Object pack, String userId, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(
                userId,
                UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC,  // 4005 - 用户在线状态更改通知同步
                pack,
                clientInfo);
    }

    private void dispatcher(Object pack, String userId, Integer appId) {
        // 获取所有的好友id
        List<String> allFriendId = imFriendService.getAllFriendId(userId, appId);
        // 遍历发送给好友通知
        for (String fid : allFriendId) {
            messageProducer.sendToUser(
                    fid,
                    UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY, // 用户在线状态更改通知
                    pack,
                    appId
            );
        }

        // TODO 发送给临时订阅的人
        String userKey = appId + ":" + Constants.RedisConstants.subscribe  + ":" + userId;
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        for (Object key : keys) {
            String filed = (String) key;
            Long expire = Long.valueOf((String) stringRedisTemplate.opsForHash().get(userKey, filed));
            if (expire > 0 && expire > System.currentTimeMillis()) {
                messageProducer.sendToUser(
                        filed,
                        UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        pack,
                        appId
                );
            } else {
                stringRedisTemplate.opsForHash().delete(userKey, filed);
            }
        }
    }

}
