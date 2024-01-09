package com.learn.im.service.seq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author lee
 * @description
 */
@Service
public class RedisSeq {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public long doGetSeq(String key){
        // 将存储为键下字符串值的整数值增加一
        return stringRedisTemplate.opsForValue().increment(key);
    }

}
