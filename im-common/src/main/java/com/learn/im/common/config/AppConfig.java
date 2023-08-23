package com.learn.im.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: lee
 * @description:
 **/
@Data
@Component
@ConfigurationProperties(prefix = "appconfig")
public class AppConfig {

    /**
     * zk连接地址
     */
    private String zkAddr;

    /**
     * zk连接超时时间
     */
    private Integer zkConnectTimeOut;

    /**
     * im管道地址路由策略
     */
    private Integer imRouteWay;

    /**
     * 如果选用一致性hash的话具体hash算法
     */
    private Integer consistentHashWay;

    /**
     * 回调Url
     */
    private String callbackUrl;

}
