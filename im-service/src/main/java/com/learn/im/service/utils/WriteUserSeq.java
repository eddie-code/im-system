package com.learn.im.service.utils;

import com.learn.im.common.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author lee
 * @description 写入用户序列
 */
@Service
public class WriteUserSeq {

    //redis hset实现
    //uid friend 10
    //    group 12
    //    conversation 123
    @Autowired
    RedisTemplate redisTemplate;

    public void writeUserSeq(Integer appId, String userId, String type, Long seq) {
        String key = appId + ":" + Constants.RedisConstants.SeqPrefix + ":" + userId;
        redisTemplate.opsForHash().put(key, type, seq);
    }

}
