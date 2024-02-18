package com.learn.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.learn.im.common.config.AppConfig;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.ConversationTypeEnum;
import com.learn.im.common.enums.DelFlagEnum;
import com.learn.im.common.model.GroupChatMessageContent;
import com.learn.im.common.model.MessageContent;
import com.learn.im.common.model.message.DoStoreGroupMessageDto;
import com.learn.im.common.model.message.DoStoreP2PMessageDto;
import com.learn.im.common.model.message.ImMessageBody;
import com.learn.im.common.model.message.OfflineMessageContent;
import com.learn.im.service.conversation.service.ConversationService;
import com.learn.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.learn.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.learn.im.service.message.dao.ImMessageBodyEntity;
import com.learn.im.service.message.dao.ImMessageHistoryEntity;
import com.learn.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.learn.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.learn.im.service.utils.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    AppConfig appConfig;

    @Autowired
    ConversationService conversationService;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {
//        // messageContent 转换成 messageBody
//        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
//        // 插入 messageBody
//        imMessageBodyMapper.insert(imMessageBodyEntity);
//        // 转化成 messageHistory (写扩散)
//        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(messageContent, imMessageBodyEntity);
//        // MP提供的方法 - 批量插入
//        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
//        // MessageKey 返回给调用者
//        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());

        ImMessageBody imMessageBodyEntity = extractMessageBody(messageContent);
        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        dto.setMessageContent(messageContent);
        dto.setMessageBody(imMessageBodyEntity);
        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
        // 往MQ推送消息
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreP2PMessage,"", JSONObject.toJSONString(dto));
    }

//    public ImMessageBodyEntity extractMessageBody(MessageContent messageContent) {
//        ImMessageBodyEntity messageBody = new ImMessageBodyEntity();
//        messageBody.setAppId(messageContent.getAppId());
//        messageBody.setMessageKey(snowflakeIdWorker.nextId()); // 雪花算法
//        messageBody.setCreateTime(System.currentTimeMillis());
//        messageBody.setSecurityKey(""); // 有需要在添加
//        messageBody.setExtra(messageContent.getExtra());
//        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode()); // 正常
//        messageBody.setMessageTime(messageContent.getMessageTime());
//        messageBody.setMessageBody(messageContent.getMessageBody());
//        return messageBody;
//    }

    public ImMessageBody extractMessageBody(MessageContent messageContent) {
        ImMessageBody messageBody = new ImMessageBody();
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

    @Transactional
    public void storeGroupMessage(GroupChatMessageContent messageContent) {
//        // messageContent 转换成 messageBody
//        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
//        // 插入 messageBody
//        imMessageBodyMapper.insert(imMessageBodyEntity);
//        // 转化成 messageHistory
//        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, imMessageBodyEntity);
//        // 插入数据
//        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
//        // MessageKey 返回给调用者
//        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());

        ImMessageBody imMessageBody = extractMessageBody(messageContent);
        DoStoreGroupMessageDto dto = new DoStoreGroupMessageDto();
        dto.setMessageBody(imMessageBody);
        dto.setGroupChatMessageContent(messageContent);
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreGroupMessage, "", JSONObject.toJSONString(dto));
        messageContent.setMessageKey(imMessageBody.getMessageKey());

    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent messageContent , ImMessageBodyEntity messageBodyEntity){
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent,result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }

    /**
     * 设置来自消息Id缓存的消息
     */
//    public void setMessageFromMessageIdCache(MessageContent messageContent) {
//        //appid : cache : messageId
//        String key = messageContent.getAppId() + ":" + Constants.RedisConstants.cacheMessage + ":" + messageContent.getMessageId();
//        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(messageContent), 300, TimeUnit.SECONDS);
//    }

//    public MessageContent getMessageFromMessageIdCache(Integer appId, String messageId) {
//        //appid : cache : messageId
//        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
//        String msg = stringRedisTemplate.opsForValue().get(key);
//        if (StringUtils.isBlank(msg)) {
//            return null;
//        }
//        return JSONObject.parseObject(msg, MessageContent.class);
//    }

    public void setMessageFromMessageIdCache(Integer appId,String messageId,Object messageContent) {
        //appid : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(messageContent), 300, TimeUnit.SECONDS);
    }

    public <T> T getMessageFromMessageIdCache(Integer appId, String messageId, Class<T> clazz) {
        //appid : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(msg)) {
            return null;
        }
        return JSONObject.parseObject(msg, clazz);
    }


    /**
     * 存储单人离线消息
     *
     * @param offlineMessage
     */
    public void storeOfflineMessage(OfflineMessageContent offlineMessage) {

        // 找到fromId的队列
        String fromKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getFromId();
        // 找到toId的队列
        String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getToId();

        // 使用zSet原因：需要分页+有序
        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        //判断 队列中的数据是否超过设定值
        if (operations.zCard(fromKey) > appConfig.getOfflineMessageCount()) {
            // 删除第一个元素
            operations.removeRange(fromKey, 0, 0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getFromId(),offlineMessage.getToId()
        ));
        // 插入数据到Redis.ZSET里面 根据messageKey 作为分值
        operations.add(
                fromKey,
                JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey()
        );
        //判断 队列中的数据是否超过设定值
        if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
            // 删除第一个元素
            operations.removeRange(toKey, 0, 0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getToId(),offlineMessage.getFromId()
        ));
        // 插入数据到Redis.ZSET里面 根据messageKey 作为分值
        operations.add(
                toKey,
                JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey()
        );

    }


    /**
     * 存储群组离线消息
     *
     * @param offlineMessage
     */
    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage, List<String> memberIds) {

        // 使用zSet原因：需要分页+有序
        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        //判断 队列中的数据是否超过设定值
        offlineMessage.setConversationType(ConversationTypeEnum.GROUP.getCode());

        for (String memberId : memberIds) {
            // 找到toId的队列
            String toKey = offlineMessage.getAppId() + ":" +
                    Constants.RedisConstants.OfflineMessage + ":" +
                    memberId;

            offlineMessage.setConversationId(conversationService.convertConversationId(
                    ConversationTypeEnum.GROUP.getCode(),
                    memberId,
                    offlineMessage.getToId()
            ));
            if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
                operations.removeRange(toKey, 0, 0);
            }
            // 插入 数据 根据messageKey 作为分值
            operations.add(
                    toKey,
                    JSONObject.toJSONString(offlineMessage),
                    offlineMessage.getMessageKey()
            );
        }
    }

}
