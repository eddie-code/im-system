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

}
