package com.learn.im.common.route.algorithm.consistenthash;

import com.learn.im.common.route.RouteHandle;

import java.util.List;


/**
 * @author lee
 * @description
 */
public class ConsistentHashHandle implements RouteHandle {

    //TreeMap
    private AbstractConsistentHash hash;

    public void setHash(AbstractConsistentHash hash) {
        this.hash = hash;
    }

    /**
     *
     * @param values 批量服务器地址
     * @param key    userId
     * @return
     */
    @Override
    public String routeServer(List<String> values, String key) {
        // 存在线程安全问题, 返回方法需要打上 synchronized()
        return hash.process(values,key);
    }
}