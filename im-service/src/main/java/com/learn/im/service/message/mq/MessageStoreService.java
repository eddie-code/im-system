package com.learn.im.service.message.mq;

import com.learn.im.common.enums.DelFlagEnum;
import com.learn.im.common.model.MessageContent;
import com.learn.im.service.message.dao.ImMessageBodyEntity;
import com.learn.im.service.message.dao.ImMessageHistoryEntity;
import com.learn.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.learn.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.learn.im.service.utils.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lee
 * @description 持久化聊天记录
 */
@Slf4j
@Service
public class MessageStoreService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;


    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {
        // messageContent 转换成 messageBody
        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
        // 插入 messageBody
        imMessageBodyMapper.insert(imMessageBodyEntity);
        // 转化成 messageHistory (写扩散)
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(messageContent, imMessageBodyEntity);
        // MP提供的方法 - 批量插入
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
        // MessageKey 返回给调用者
        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
    }

    public ImMessageBodyEntity extractMessageBody(MessageContent messageContent) {
        ImMessageBodyEntity messageBody = new ImMessageBodyEntity();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(snowflakeIdWorker.nextId()); // 雪花算法
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey(""); // 有需要在添加
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode()); // 正常
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;
    }

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent, ImMessageBodyEntity imMessageBodyEntity) {
        List<ImMessageHistoryEntity> list = new ArrayList<>();

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());  // OwnerI = FromId
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId()); // OwnerI = ToId
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }

}
