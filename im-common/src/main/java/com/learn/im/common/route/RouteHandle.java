package com.learn.im.common.route;

import java.util.List;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
public interface RouteHandle {

    /**
     * @param values 批量服务器地址
     * @param key    根据key获取
     * @return
     */
    String routeServer(List<String> values, String key);

}
