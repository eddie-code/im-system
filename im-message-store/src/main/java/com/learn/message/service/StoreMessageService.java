package com.learn.message.service;

import com.learn.im.common.model.GroupChatMessageContent;
import com.learn.im.common.model.MessageContent;
import com.learn.message.dao.ImGroupMessageHistoryEntity;
import com.learn.message.dao.ImMessageBodyEntity;
import com.learn.message.dao.ImMessageHistoryEntity;
import com.learn.message.dao.mapper.ImGroupMessageHistoryMapper;
import com.learn.message.dao.mapper.ImMessageBodyMapper;
import com.learn.message.dao.mapper.ImMessageHistoryMapper;
import com.learn.message.model.DoStoreGroupMessageDto;
import com.learn.message.model.DoStoreP2PMessageDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lee
 * @description
 */
@Service
public class StoreMessageService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Transactional
    public void doStoreP2PMessage(DoStoreP2PMessageDto doStoreP2PMessageDto) {
        imMessageBodyMapper.insert(doStoreP2PMessageDto.getImMessageBodyEntity());
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(doStoreP2PMessageDto.getMessageContent(), doStoreP2PMessageDto.getImMessageBodyEntity());
        // 写扩散 (批量插入)
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
    }

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent, ImMessageBodyEntity imMessageBodyEntity) {
        List<ImMessageHistoryEntity> list = new ArrayList<>();
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());
        // 序列号
        fromHistory.setSequence(messageContent.getMessageSequence());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());
        // 序列号
        toHistory.setSequence(messageContent.getMessageSequence());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }

    @Transactional
    public void doStoreGroupMessage(DoStoreGroupMessageDto doStoreGroupMessageDto) {
        imMessageBodyMapper.insert(doStoreGroupMessageDto.getImMessageBodyEntity());
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(doStoreGroupMessageDto.getGroupChatMessageContent(), doStoreGroupMessageDto.getImMessageBodyEntity());
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent messageContent, ImMessageBodyEntity messageBodyEntity) {
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }

}
