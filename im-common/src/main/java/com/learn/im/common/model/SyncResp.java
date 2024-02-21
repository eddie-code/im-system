package com.learn.im.common.model;

import lombok.Data;

import java.util.List;

/**
 * @author: lee
 * @description: 返回给客户端的值
 **/
@Data
public class SyncResp<T> {

    /**
     * 本次最大的Seq
     */
    private Long maxSequence;

    /**
     * 是否拉取成功
     */
    private boolean isCompleted;

    /**
     * 数据集
     */
    private List<T> dataList;

}
