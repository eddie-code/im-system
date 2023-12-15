package com.learn.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.ImConnectStatusEnum;
import com.learn.im.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@Component
public class UserSessionUtils {

    public Object get;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 1.获取用户所有的session
     */
    public List<UserSession> getUserSession(Integer appId, String userId) {
        // appId:userSession:userId
        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants + userId;
        // 获取所有的session
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(userSessionKey);
        // 返回值
        List<UserSession> list = new ArrayList<>();
        // 所有的session value
        Collection<Object> values = entries.values();
        for (Object o : values) {
            String str = (String) o;
            // 解析对象
            UserSession session = JSONObject.parseObject(str, UserSession.class);
            // 连接状态 1=在线 2=离线
            if (session.getConnectState() == ImConnectStatusEnum.ONLINE_STATUS.getCode()) {
                // 在线的才加入集合
                list.add(session);
            }
        }
        return list;
    }

    /**
     * 2.获取用户除了本端的session (通过clientType + imei获取指定端的session)
     */
    public UserSession getUserSession(Integer appId, String userId, Integer clientType, String imei) {
        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants + userId;
        String hashKey = clientType + ":" + imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);
        UserSession session = JSONObject.parseObject(o.toString(), UserSession.class);
        return session;
    }


}
