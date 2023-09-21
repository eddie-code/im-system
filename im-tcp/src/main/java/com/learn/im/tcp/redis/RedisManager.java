package com.learn.im.tcp.redis;

import com.learn.im.codec.config.BootstrapConfig;
import com.learn.im.tcp.reciver.UserLoginMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@Slf4j
public class RedisManager {

    private static RedissonClient redissonClient;

    private static Integer loginModel;

    public static void init(BootstrapConfig config){
        loginModel = config.getLee().getLoginModel();
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getLee().getRedis());
        // 初始化多端登录的监听类
        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginModel);
        userLoginMessageListener.listenerUserLogin();
        log.info("Redis初始化：loginModel={}, redis={}", loginModel, config.getLee().getRedis());
    }

    public static RedissonClient getRedissonClient() {
        return redissonClient;
    }

}
