package com.learn.im.common.model;

import lombok.Data;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@Data
public class UserSession {

    private String userId;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 端的标识
     */
    private Integer clientType;

    //sdk 版本号
    private Integer version;

    //连接状态 1=在线 2=离线
    private Integer connectState;

    /**
     * 区别那个服务器id
     */
    private Integer brokerId;

    /**
     * 服务器IP
     */
    private String brokerHost;

    private String imei;

}
