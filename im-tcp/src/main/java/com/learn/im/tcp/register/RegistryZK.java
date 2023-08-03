package com.learn.im.tcp.register;

import com.learn.im.codec.config.BootstrapConfig;
import com.learn.im.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lee
 * @description
 */
@Slf4j
public class RegistryZK implements Runnable {

    private ZKit zKit;

    private String ip;

    private BootstrapConfig.TcpConfig tcpConfig;

    public RegistryZK(ZKit zKit, String ip, BootstrapConfig.TcpConfig tcpConfig) {
        this.zKit = zKit;
        this.ip = ip;
        this.tcpConfig = tcpConfig;
    }

    @Override
    public void run() {
        zKit.createRootNode();
        // 拼接 /im-coreRoot/tcp/192.168.8.250:9000
        String tcpPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp + "/" + ip + ":" + tcpConfig.getTcpPort();
        zKit.createNode(tcpPath);
        log.info("Registry zookeeper tcpPath success, msg=[{}]", tcpPath);

        // 拼接 /im-coreRoot/tcp/192.168.8.250:9000
        String webPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb + "/" + ip + ":" + tcpConfig.getWebSocketPort();
        zKit.createNode(webPath);
        log.info("Registry zookeeper webPath success, msg=[{}]", tcpPath);
    }

}
