package com.learn.im.common.enums.command;

/**
 * @author lee
 * @description
 */
public enum SystemCommand implements Command {

    /**
     * 登录 9000
     */
    LOGIN(0x2328),

    ;

    private int command;

    SystemCommand(int command) {
        this.command = command;
    }

    @Override
    public int getCommand() {
        return command;
    }

}
