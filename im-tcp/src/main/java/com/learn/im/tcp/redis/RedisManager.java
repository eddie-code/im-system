package com.learn.im.tcp.redis;

import com.learn.im.codec.config.BootstrapConfig;
import org.redisson.api.RedissonClient;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
public class RedisManager {

    private static RedissonClient redissonClient;

    private static Integer loginModel;

    public static void init(BootstrapConfig config){
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getLee().getRedis());
    }

    public static RedissonClient getRedissonClient() {
        return redissonClient;
    }

}
