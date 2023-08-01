package com.learn.im.common.model;

import lombok.Data;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@Data
public class UserClientDto {

    /**
     * appId
     */
    private Integer appId;

    /**
     * 端
     */
    private Integer clientType;

    /**
     * 用户id
     */
    private String userId;

    /**
     * imei号
     */
    private String imei;

}
