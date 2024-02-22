package com.learn.im.common.enums.command;

/**
 * @author lee
 * @description
 */
public enum UserEventCommand implements Command {

    //用户修改command 4000
    USER_MODIFY(4000),

    //4001 用户状态修改报文 tcp服务发给逻辑层
    USER_ONLINE_STATUS_CHANGE(4001),

    // ------------ 逻辑层发送给客户端 --------------

    //4004 用户在线状态通知报文 (发给订阅或者好友的）
    USER_ONLINE_STATUS_CHANGE_NOTIFY(4004),

    //4005 用户在线状态通知同步报文  (发给同步端）
    USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC(4005),

    // ------------ 逻辑层发送给客户端 --------------

    ;

    private int command;

    UserEventCommand(int command){
        this.command=command;
    }


    @Override
    public int getCommand() {
        return command;
    }
}
