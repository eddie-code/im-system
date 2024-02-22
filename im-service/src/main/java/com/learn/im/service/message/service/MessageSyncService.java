package com.learn.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.learn.im.codec.pack.message.MessageReadedPack;
import com.learn.im.common.ResponseVO;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.command.Command;
import com.learn.im.common.enums.command.GroupEventCommand;
import com.learn.im.common.enums.command.MessageCommand;
import com.learn.im.common.model.SyncReq;
import com.learn.im.common.model.SyncResp;
import com.learn.im.common.model.message.MessageReadedContent;
import com.learn.im.common.model.message.MessageReciveAckContent;
import com.learn.im.common.model.message.OfflineMessageContent;
import com.learn.im.service.conversation.service.ConversationService;
import com.learn.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author lee
 * @description
 */
@Service
public class MessageSyncService {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ConversationService conversationService;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 消息接收确认
     */
    public void receiveMark(MessageReciveAckContent messageReciveAckContent) {
        messageProducer.sendToUser(messageReciveAckContent.getToId(),
                MessageCommand.MSG_RECIVE_ACK,
                messageReciveAckContent,
                messageReciveAckContent.getAppId()
        );
    }

    /**
     * 消息已读。更新会话的seq，通知在线的同步端发送指定command ，发送已读回执通知对方（消息发起方）我已读
     */
    public void readMark(MessageReadedContent messageReadedContent) {

        conversationService.messageMarkRead(messageReadedContent);

        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReadedContent, messageReadedPack);
        //消息已读通知给同步端 1053
        syncToSender(messageReadedPack, messageReadedContent, MessageCommand.MSG_READED_NOTIFY);

        // 发送给所有端的方法
        messageProducer.sendToUser(messageReadedContent.getToId(), MessageCommand.MSG_READED_RECEIPT, messageReadedPack, messageReadedContent.getAppId());
    }

    /**
     * 发送给tcp服务的数据包，都写在 im-codec 里面
     */
    private void syncToSender(MessageReadedPack pack, MessageReadedContent messageReadedContent, Command command) {

        MessageReadedPack messageReadedPack = new MessageReadedPack();
//        BeanUtils.copyProperties(messageReadedContent, messageReadedPack);

        //发送给自己的其他端
        messageProducer.sendToUserExceptClient(messageReadedContent.getFromId(), command, pack, messageReadedContent);
    }

    public void groupReadMark(MessageReadedContent messageReaded) {
        conversationService.messageMarkRead(messageReaded);
        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReaded, messageReadedPack);
        //消息已读通知给同步端 1053
        syncToSender(messageReadedPack, messageReaded, GroupEventCommand.MSG_GROUP_READED_NOTIFY);
        if (!messageReaded.getFromId().equals(messageReaded.getToId())) {
            messageProducer.sendToUser(messageReadedPack.getToId(), GroupEventCommand.MSG_GROUP_READED_RECEIPT, messageReaded, messageReaded.getAppId());
        }
    }

    public ResponseVO syncOfflineMessage(SyncReq req) {

        // 返回值
        SyncResp<OfflineMessageContent> resp = new SyncResp<>();

        // 10000:offlineMessage:lld
        String key = req.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + req.getOperater();

        // 获取最大的seq
        Long maxSeq = 0L;
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set set = zSetOperations.reverseRangeWithScores(key, 0, 0);
        if (!CollectionUtils.isEmpty(set)) {
            // 这种方式会保留Set中元素的顺序, 并将其添加到ArrayList中
            List list = new ArrayList(set);
            // redise 默认返回的tuple是DefaultTypedTuple
            DefaultTypedTuple o = (DefaultTypedTuple) list.get(0);
            maxSeq = o.getScore().longValue();
        }
        resp.setMaxSequence(maxSeq);

        List<OfflineMessageContent> respList = new ArrayList<>();
        // 根据传入的 lastSequence 的值和 最大值maxSeq 进行范围查询， 从第一条获取到传入限制最大条数maxLimit的范围
        Set<ZSetOperations.TypedTuple> querySet = zSetOperations.rangeByScoreWithScores(key, req.getLastSequence(), maxSeq, 0, req.getMaxLimit());
        for (ZSetOperations.TypedTuple<String> typedTuple : querySet) {
            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            respList.add(offlineMessageContent);
        }
        resp.setDataList(respList);

        if (!CollectionUtils.isEmpty(respList)) {
            // 最后一个元素
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            // 判断是否拉取成功
            resp.setCompleted(maxSeq <= offlineMessageContent.getMessageKey());  // 小于等于
        }

        return ResponseVO.successResponse(resp);
    }

}
