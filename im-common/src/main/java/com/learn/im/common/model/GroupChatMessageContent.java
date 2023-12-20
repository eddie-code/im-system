package com.learn.im.common.model;

import lombok.Data;

import java.util.List;

/**
 * @author lee
 * @description
 */
@Data
public class GroupChatMessageContent extends MessageContent {

    private String groupId;

    private List<String> memberId;

}