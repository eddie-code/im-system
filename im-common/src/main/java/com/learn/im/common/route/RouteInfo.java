package com.learn.im.common.route;

import lombok.Data;

/**
 * @author lee
 */

@Data
public final class RouteInfo {

    private String ip;
    private Integer port;

    public RouteInfo(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }
}
