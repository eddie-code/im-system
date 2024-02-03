package com.learn.im.common.enums.command;

/**
 * @author lee
 * @description
 */
public enum ConversationEventCommand implements Command {

    //删除会话
    CONVERSATION_DELETE(5000),

    //删除会话
    CONVERSATION_UPDATE(5001),

    ;

    private int command;

    ConversationEventCommand(int command) {
        this.command = command;
    }


    @Override
    public int getCommand() {
        return command;
    }
}
