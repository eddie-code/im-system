package com.learn.im.tcp.register;

import com.learn.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;

/**
 * @author lee
 * @description 生成zk节点
 */
public class ZKit {

    private ZkClient zkClient;

    public ZKit(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    /**
     * 创建根节点
     * im-coreRoot/tcp/ip:port
     */
    public void createRootNode() {
        // 判断Root节点是否存在
        boolean exists = zkClient.exists(Constants.ImCoreZkRoot);
        if (!exists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot);
        }

        // tcp 节点是否存在
        boolean tcpExists = zkClient.exists(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        if (!tcpExists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        }

        // websocket 节点是否存在
        boolean webExists = zkClient.exists(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        if (!tcpExists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        }
    }

    /**
     * 创建节点
     * ip+port
     */
    public void createNode(String path) {
        if (!zkClient.exists(path)) {
            zkClient.createPersistent(path);
        }
    }

}
