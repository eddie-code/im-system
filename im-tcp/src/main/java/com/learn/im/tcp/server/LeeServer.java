package com.learn.im.tcp.server;

import com.learn.im.codec.MessageDecoder;
import com.learn.im.codec.config.BootstrapConfig;
import com.learn.im.tcp.handler.HeartBeatHandler;
import com.learn.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lee
 * @description Netty Server
 */
@Slf4j
public class LeeServer {

    BootstrapConfig.TcpConfig config;

    EventLoopGroup mainGroup;

    EventLoopGroup subGroup;

    ServerBootstrap server;

    public LeeServer(BootstrapConfig.TcpConfig config) {
        this.config = config;
        // 线程池
        mainGroup = new NioEventLoopGroup(config.getBossThreadSize());
        subGroup = new NioEventLoopGroup(config.getWorkThreadSize());
        // 赋值给server
        server = new ServerBootstrap();
        server.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240) // 服务端可连接队列大小
                .option(ChannelOption.SO_REUSEADDR, true) // 参数表示允许重复使用本地地址和端口
                .childOption(ChannelOption.TCP_NODELAY, true) // 是否禁用Nagle算法 简单点说是否批量发送数据 true关闭 false开启。 开启的话可以减少一定的网络开销，但影响消息实时性
                .childOption(ChannelOption.SO_KEEPALIVE, true) // 保活开关2h没有数据服务端会发送心跳包
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new MessageDecoder());
                        // 超过十秒钟就触发一次检测
                        socketChannel.pipeline().addLast(new IdleStateHandler(0, 0, 10));
                        socketChannel.pipeline().addLast(new HeartBeatHandler(config.getHeartBeatTime()));
                        socketChannel.pipeline().addLast(new NettyServerHandler());
                    }
                });
    }

    public void start() {
        this.server.bind(this.config.getTcpPort());
    }


}
