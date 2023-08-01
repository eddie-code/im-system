package com.learn.im.tcp.handler;

import com.learn.im.common.constant.Constants;
import com.learn.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lee
 * @description 心跳检测处理器 - 通道入站处理程序适配器
 */
@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private Long heartBeatTime;

    public HeartBeatHandler(Long heartBeatTime) {
        this.heartBeatTime = heartBeatTime;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;        // 强制类型转换
            if (event.state() == IdleState.READER_IDLE) {
                log.info("读空闲");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                log.info("进入写空闲");
            } else if (event.state() == IdleState.ALL_IDLE) {
                Long lastReadTime = (Long) ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).get();
                long now = System.currentTimeMillis();

                // 读取时间不等于空 并且 当前时间 减去 读取时间 需要大于 自定义的超时时间
                if (lastReadTime != null && now - lastReadTime > heartBeatTime) {
                    // TODO 退出后台逻辑
                    SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
                }
            }
        }
    }
}
