package com.learn.im.tcp;

import com.learn.im.tcp.server.LeeServer;
import com.learn.im.tcp.server.LeeWebSocketServer;

/**
 * @author lee
 * @description
 */
public class Starter {

    public static void main(String[] args) {
        new LeeServer(9000);
        new LeeWebSocketServer(19000);
    }

}
