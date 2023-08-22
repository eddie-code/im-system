package com.learn.im.service.config;

import com.learn.im.common.config.AppConfig;
import com.learn.im.common.route.RouteHandle;
import com.learn.im.common.route.algorithm.consistenthash.ConsistentHashHandle;
import com.learn.im.common.route.algorithm.consistenthash.TreeMapConsistentHash;
import com.learn.im.common.route.algorithm.loop.LoopHandle;
import com.learn.im.common.route.algorithm.random.RandomHandle;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@Configuration
public class BeanConfig {

    @Autowired
    private AppConfig appConfig;

    @Bean
    public ZkClient buildZKClient() {
        return new ZkClient(appConfig.getZkAddr(), appConfig.getZkConnectTimeOut());
    }

//    @Bean
//    public RouteHandle routeHandle() {
//        return new RandomHandle();
//    }

//    @Bean
//    public RouteHandle routeHandle() {
//        return new LoopHandle();
//    }

    @Bean
    public RouteHandle routeHandle() {
        ConsistentHashHandle consistentHashHandle = new ConsistentHashHandle();
        TreeMapConsistentHash treeMapConsistentHash = new TreeMapConsistentHash();
        consistentHashHandle.setHash(treeMapConsistentHash);
        return consistentHashHandle;
    }

}
