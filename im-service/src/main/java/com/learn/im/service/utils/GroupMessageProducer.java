package com.learn.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.learn.im.codec.pack.group.AddGroupMemberPack;
import com.learn.im.codec.pack.group.RemoveGroupMemberPack;
import com.learn.im.codec.pack.group.UpdateGroupMemberPack;
import com.learn.im.common.ClientType;
import com.learn.im.common.enums.command.Command;
import com.learn.im.common.enums.command.GroupEventCommand;
import com.learn.im.common.model.ClientInfo;
import com.learn.im.service.group.model.req.GroupMemberDto;
import com.learn.im.service.group.service.ImGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@Component
public class GroupMessageProducer {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    public void producer(String userId, Command command, Object data, ClientInfo clientInfo) {
        JSONObject o = (JSONObject) JSONObject.toJSON(data);
        String groupId = o.getString("groupId");
        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());

        for (String memberId : groupMemberId) {
            // 是否app发起的请求 && memberId是否当前自己
            if (clientInfo.getClientType() != null && clientInfo.getClientType() != ClientType.WEBAPI.getCode() && memberId.equals(userId)) {
                messageProducer.sendToUserExceptClient(memberId, command, data, clientInfo);
            } else {
                messageProducer.sendToUser(memberId, command, data, clientInfo.getAppId());
            }
        }

    }

}
