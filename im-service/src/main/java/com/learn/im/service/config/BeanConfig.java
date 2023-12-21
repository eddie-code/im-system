package com.learn.im.service.config;

import com.learn.im.common.config.AppConfig;
import com.learn.im.common.enums.ImUrlRouteWayEnum;
import com.learn.im.common.enums.RouteHashMethodEnum;
import com.learn.im.common.route.RouteHandle;
import com.learn.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import com.learn.im.common.route.algorithm.consistenthash.ConsistentHashHandle;
import com.learn.im.common.route.algorithm.consistenthash.TreeMapConsistentHash;
import com.learn.im.common.route.algorithm.loop.LoopHandle;
import com.learn.im.common.route.algorithm.random.RandomHandle;
import com.learn.im.service.utils.SnowflakeIdWorker;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

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

//    @Bean
//    public RouteHandle routeHandle() {
//        ConsistentHashHandle consistentHashHandle = new ConsistentHashHandle();
//        TreeMapConsistentHash treeMapConsistentHash = new TreeMapConsistentHash();
//        consistentHashHandle.setHash(treeMapConsistentHash);
//        return consistentHashHandle;
//    }

    @Bean
    public RouteHandle routeHandle() throws Exception {

        // 1. 读取配置信息，获取IM路由方式的值
        Integer imRouteWay = appConfig.getImRouteWay();
        String routWay = "";

        // 2. 根据枚举值获取对应的绝对路径的类名
        ImUrlRouteWayEnum handler = ImUrlRouteWayEnum.getHandler(imRouteWay);
        routWay = handler.getClazz();

        // 3. 使用反射创建该类的实例。
        RouteHandle routeHandle = (RouteHandle) Class.forName(routWay).newInstance();

        // 4. 如果路由方式是一致性哈希
        if (handler == ImUrlRouteWayEnum.HASH) {
            // a. 反射获取设置哈希方式的方法名称
            Method setHash = Class.forName(routWay).getMethod("setHash", AbstractConsistentHash.class);
            // b. 读取配置信息，获取一致性哈希方式的值
            Integer consistentHashWay = appConfig.getConsistentHashWay();
            String hashWay = "";
            // c. 根据枚举值获取对应的绝对路径的类名
            RouteHashMethodEnum hashHandler = RouteHashMethodEnum.getHandler(consistentHashWay);
            assert hashHandler != null;
            // d. 使用反射创建该类的实例
            hashWay = hashHandler.getClazz();
            AbstractConsistentHash consistentHash = (AbstractConsistentHash) Class.forName(hashWay).newInstance();
            // e. 调用设置哈希方式的方法，将一致性哈希实例设置到路由处理器中
            setHash.invoke(routeHandle, consistentHash);
        }
        // 5. 返回路由处理器实例
        return routeHandle;
    }

    @Bean
    public EasySqlInjector easySqlInjector () {
        return new EasySqlInjector();
    }

    @Bean
    public SnowflakeIdWorker buildSnowflakeSeq() throws Exception {
        return new SnowflakeIdWorker(0);
    }

}
