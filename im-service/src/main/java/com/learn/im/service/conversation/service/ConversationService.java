package com.learn.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.learn.im.codec.pack.conversation.DeleteConversationPack;
import com.learn.im.codec.pack.conversation.UpdateConversationPack;
import com.learn.im.common.ResponseVO;
import com.learn.im.common.config.AppConfig;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.ConversationErrorCode;
import com.learn.im.common.enums.ConversationTypeEnum;
import com.learn.im.common.enums.command.ConversationEventCommand;
import com.learn.im.common.model.ClientInfo;
import com.learn.im.common.model.message.MessageReadedContent;
import com.learn.im.service.conversation.dao.ImConversationSetEntity;
import com.learn.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.learn.im.service.conversation.model.DeleteConversationReq;
import com.learn.im.service.conversation.model.UpdateConversationReq;
import com.learn.im.service.seq.RedisSeq;
import com.learn.im.service.utils.MessageProducer;
import com.learn.im.service.utils.WriteUserSeq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lee
 * @description
 */
@Slf4j
@Service
public class ConversationService {

    @Autowired
    ImConversationSetMapper imConversationSetMapper;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    AppConfig appConfig;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

    public String convertConversationId(Integer type, String fromId, String toId) {
        return type + "_" + fromId + "_" + toId;
    }

    public void messageMarkRead(MessageReadedContent messageReadedContent) {

        String toId = messageReadedContent.getToId();
        // 0 单聊 1群聊 2机器人 3公众号
        if (messageReadedContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()) {
            toId = messageReadedContent.getGroupId();
        }

        String conversationId = convertConversationId(
                messageReadedContent.getConversationType(), messageReadedContent.getFromId(), toId
        );
        log.info("conversationId: {}", conversationId);
        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id", conversationId);
        query.eq("app_id", messageReadedContent.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
        if (imConversationSetEntity == null) {
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity = new ImConversationSetEntity();
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent, imConversationSetEntity);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.insert(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(), messageReadedContent.getFromId(),Constants.SeqConstants.Conversation,seq);
        } else {
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setSequence(seq);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);
            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(), messageReadedContent.getFromId(),Constants.SeqConstants.Conversation,seq);
        }
    }

    /**
     * 删除会话
     *
     * @param req
     * @return
     */
    public ResponseVO deleteConversation(DeleteConversationReq req) {

        //置顶 有免打扰 (根据产品需求, 此步骤可有可无)
//        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("conversation_id", req.getConversationId());
//        queryWrapper.eq("app_id", req.getAppId());
//        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
//        if (imConversationSetEntity != null) {
//            // 是否禁音
//            imConversationSetEntity.setIsMute(0);
//            // 是否置顶
//            imConversationSetEntity.setIsTop(0);
//            imConversationSetMapper.update(imConversationSetEntity, queryWrapper);
//        }

        if (appConfig.getDeleteConversationSyncMode() == 1) {
            DeleteConversationPack pack = new DeleteConversationPack();
            // 设置会话Id
            pack.setConversationId(req.getConversationId());
            // 多端同步请求, 发送给同步端
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_DELETE,
                    pack,
                    new ClientInfo(req.getAppId(), req.getClientType(), req.getImei())
            );
        }

        return ResponseVO.successResponse();
    }

    /**
     * 更新会话 置顶or免打扰
     *
     * @param req
     * @return
     */
    public ResponseVO updateConversation(UpdateConversationReq req) {

        if (req.getIsTop() == null && req.getIsMute() == null) {
            // 会话修改参数错误
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", req.getConversationId());
        queryWrapper.eq("app_id", req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if (imConversationSetEntity != null) {

            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Conversation);

            if (req.getIsMute() != null) {
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            if (req.getIsMute() != null) {
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.update(imConversationSetEntity, queryWrapper);

            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Conversation, seq);

            // 若更新会话（置顶or免打扰）, 更新到同步端
            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setSequence(seq);
            pack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_UPDATE,
                    pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei())
            );
        }
        return ResponseVO.successResponse();
    }
}
