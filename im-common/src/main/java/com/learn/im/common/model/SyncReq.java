package com.learn.im.common.model;

import lombok.Data;

/**
 * @author: lee
 * @description: 接收客户端传参
 **/
@Data
public class SyncReq extends RequestBase {

    /**
     * 客户端最大seq
     */
    private Long lastSequence;

    /**
     * 一次拉取多少
     */
    private Integer maxLimit;

}
