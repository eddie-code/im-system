package com.learn.im.service.friendship.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.learn.im.codec.pack.friendship.*;
import com.learn.im.common.ResponseVO;
import com.learn.im.common.config.AppConfig;
import com.learn.im.common.constant.Constants;
import com.learn.im.common.enums.AllowFriendTypeEnum;
import com.learn.im.common.enums.CheckFriendShipTypeEnum;
import com.learn.im.common.enums.FriendShipErrorCode;
import com.learn.im.common.enums.FriendShipStatusEnum;
import com.learn.im.common.enums.command.FriendshipEventCommand;
import com.learn.im.common.exception.ApplicationException;
import com.learn.im.common.model.RequestBase;
import com.learn.im.service.friendship.dao.ImFriendShipEntity;
import com.learn.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.learn.im.service.friendship.model.callback.AddFriendAfterCallbackDto;
import com.learn.im.service.friendship.model.callback.AddFriendBlackAfterCallbackDto;
import com.learn.im.service.friendship.model.callback.DeleteFriendAfterCallbackDto;
import com.learn.im.service.friendship.model.req.*;
import com.learn.im.service.friendship.model.resp.CheckFriendShipResp;
import com.learn.im.service.friendship.model.resp.ImportFriendShipResp;
import com.learn.im.service.friendship.service.ImFriendService;
import com.learn.im.service.friendship.service.ImFriendShipRequestService;
import com.learn.im.service.user.dao.ImUserDataEntity;
import com.learn.im.service.user.service.ImUserService;
import com.learn.im.service.utils.CallbackService;
import com.learn.im.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@Slf4j
@Service
public class ImFriendServiceImpl implements ImFriendService {

    @Autowired
    private ImFriendShipMapper imFriendShipMapper;

    @Autowired
    private ImUserService imUserService;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private CallbackService callbackService;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    @Override
    public ResponseVO importFriendShip(ImporFriendShipReq req) {

        if (req.getFriendItem().size() > 100) {
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendShipResp resp = new ImportFriendShipResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        for (ImporFriendShipReq.ImportFriendDto dto : req.getFriendItem()) {
            ImFriendShipEntity entity = new ImFriendShipEntity();
            BeanUtils.copyProperties(dto, entity);
            entity.setAppId(req.getAppId());
            entity.setFromId(req.getFromId());
            try {
                int insert = imFriendShipMapper.insert(entity);
                if (insert == 1) {
                    successId.add(dto.getToId());
                } else {
                    errorId.add(dto.getToId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorId.add(dto.getToId());
            }

        }

        resp.setErrorId(errorId);
        resp.setSuccessId(successId);

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addFriend(AddFriendReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        // 之前回调
        if (appConfig.isAddFriendBeforeCallback()) {
            ResponseVO responseVO = callbackService.beforeCallback(
                    req.getAppId(),
                    Constants.CallbackCommand.AddFriendBefore,
                    JSONObject.toJSONString(req)
            );
            // 如果远程调用失败就返回
            if (!responseVO.isOk()) {
                return responseVO;
            }
        }

        ImUserDataEntity data = toInfo.getData();

        // 判断是否好有，如是就不需要在发送添加申请
        if (data.getFriendAllowType() != null && data.getFriendAllowType() == AllowFriendTypeEnum.NOT_NEED.getCode()) {
            return this.doAddFriend(req, req.getFromId(), req.getToItem(), req.getAppId());
        } else {
            QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.eq("from_id", req.getFromId());
            query.eq("to_id", req.getToItem().getToId());
            ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
            // 是否好有结果集不能等于空 || 结果集的状态 不等于 好友状态=正常
            if (fromItem == null || fromItem.getStatus() != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                //插入一条好友申请的数据
                ResponseVO responseVO = imFriendShipRequestService.addFienshipRequest(req.getFromId(), req.getToItem(), req.getAppId());
                if (!responseVO.isOk()) {
                    return responseVO;
                }
            } else {
                // 否则提示已经是好友
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }

        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO updateFriend(UpdateFriendReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        ResponseVO responseVO = this.doUpdate(req.getFromId(), req.getToItem(), req.getAppId());

        if (responseVO.isOk()) {
            // 更新好友的tcp通知
            UpdateFriendPack updateFriendPack = new UpdateFriendPack();
            updateFriendPack.setRemark(req.getToItem().getRemark());
            updateFriendPack.setToId(req.getToItem().getToId());
            messageProducer.sendToUser(
                    req.getFromId(),
                    req.getClientType(),
                    req.getImei(),
                    FriendshipEventCommand.FRIEND_UPDATE,
                    updateFriendPack,req.getAppId()
            );

            //之后回调
            if (appConfig.isModifyFriendAfterCallback()) {
                AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToItem(req.getToItem());
                callbackService.beforeCallback(
                        req.getAppId(),
                        Constants.CallbackCommand.UpdateFriendAfter,
                        JSONObject.toJSONString(callbackDto)
                );
            }
        }

        return responseVO;
    }

    @Transactional
    public ResponseVO doUpdate(String fromId, FriendDto dto, Integer appId) {


        UpdateWrapper<ImFriendShipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(ImFriendShipEntity::getAddSource, dto.getAddSource())
                .set(ImFriendShipEntity::getExtra, dto.getExtra())
                .set(ImFriendShipEntity::getRemark, dto.getRemark())
                .eq(ImFriendShipEntity::getAppId, appId)
                .eq(ImFriendShipEntity::getToId, dto.getToId())
                .eq(ImFriendShipEntity::getFromId, fromId);

        int update = imFriendShipMapper.update(null, updateWrapper);

        if (update == 1) {
            //之后回调
//            if (appConfig.isModifyFriendAfterCallback()) {
//                AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
//                callbackDto.setFromId(fromId);
//                callbackDto.setToItem(dto);
//                callbackService.beforeCallback(
//                        appId,
//                        Constants.CallbackCommand.UpdateFriendAfter,
//                        JSONObject.toJSONString(callbackDto));
//            }
            return ResponseVO.successResponse();
        }

        return ResponseVO.errorResponse();
    }

    @Override
    @Transactional
    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId) {

        //A-B
        //Friend表插入A 和 B 两条记录
        //查询是否有记录存在，如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        query.eq("to_id", dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if (fromItem == null) {
            //走添加逻辑。
            fromItem = new ImFriendShipEntity();
            fromItem.setAppId(appId);
            fromItem.setFromId(fromId);
//            entity.setToId(to);
            BeanUtils.copyProperties(dto, fromItem);
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
        } else {
            //如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态
            if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            } else {
                ImFriendShipEntity update = new ImFriendShipEntity();

                if (StringUtils.isNotBlank(dto.getAddSource())) {
                    update.setAddSource(dto.getAddSource());
                }

                if (StringUtils.isNotBlank(dto.getRemark())) {
                    update.setRemark(dto.getRemark());
                }

                if (StringUtils.isNotBlank(dto.getExtra())) {
                    update.setExtra(dto.getExtra());
                }
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

                int result = imFriendShipMapper.update(update, query);
                if (result != 1) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
            }

            // to的好友关系链
            QueryWrapper<ImFriendShipEntity> toQuery = new QueryWrapper<>();
            toQuery.eq("app_id", appId);
            toQuery.eq("from_id", dto.getToId());
            toQuery.eq("to_id", fromId);
            ImFriendShipEntity toItem = imFriendShipMapper.selectOne(toQuery);
            if (toItem == null) {
                toItem = new ImFriendShipEntity();
                toItem.setAppId(appId);
                toItem.setFromId(dto.getToId());
                BeanUtils.copyProperties(dto, toItem);
                toItem.setToId(fromId);
                toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                toItem.setCreateTime(System.currentTimeMillis());
                toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
                int insert = imFriendShipMapper.insert(toItem);
            } else {
                // 如果状态不等于正常, 才去更新
                if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != toItem.getStatus()) {
                    ImFriendShipEntity update = new ImFriendShipEntity();
                    update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                    imFriendShipMapper.update(update, toQuery);
                }
            }

            //发送给from
            AddFriendPack addFriendPack = new AddFriendPack();
            BeanUtils.copyProperties(fromItem, addFriendPack);
            if (requestBase != null) {
                // 发送给指定端
                messageProducer.sendToUser(
                        fromId,
                        requestBase.getClientType(),
                        requestBase.getImei(),
                        FriendshipEventCommand.FRIEND_ADD,
                        addFriendPack,
                        requestBase.getAppId());
            } else {
                // 发送给所有端
                messageProducer.sendToUser(
                        fromId,
                        FriendshipEventCommand.FRIEND_ADD,
                        addFriendPack,
                        requestBase.getAppId());
            }

            // to
            AddFriendPack addFriendToPack = new AddFriendPack();
            BeanUtils.copyProperties(toItem,addFriendPack);
            messageProducer.sendToUser(
                    toItem.getFromId(),
                    FriendshipEventCommand.FRIEND_ADD,
                    addFriendToPack,
                    requestBase.getAppId()
            );

            //之后回调
            if (appConfig.isAddFriendBeforeCallback()) {
                AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
                callbackDto.setFromId(fromId);
                callbackDto.setToItem(dto);
                callbackService.beforeCallback(
                        appId,
                        Constants.CallbackCommand.AddFriendAfter,
                        JSONObject.toJSONString(callbackDto)
                );
            }
        }

        QueryWrapper<ImFriendShipEntity> toQuery = new QueryWrapper<>();
        toQuery.eq("app_id", appId);
        toQuery.eq("from_id", dto.getToId());
        toQuery.eq("to_id", fromId);
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(toQuery);
        if (toItem == null) {
            toItem = new ImFriendShipEntity();
            toItem.setAppId(appId);
            toItem.setFromId(dto.getToId());
            BeanUtils.copyProperties(dto, toItem);
            toItem.setToId(fromId);
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
//            toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            int insert = imFriendShipMapper.insert(toItem);
        } else {
            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() !=
                    toItem.getStatus()) {
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                imFriendShipMapper.update(update, toQuery);
            }
        }
        return ResponseVO.successResponse();
    }


    @Override
    public ResponseVO deleteFriend(DeleteFriendReq req) {

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if (fromItem == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        } else {
            if (fromItem.getStatus() != null && fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
                imFriendShipMapper.update(update, query);
            } else {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            DeleteFriendPack deleteFriendPack = new DeleteFriendPack();
            deleteFriendPack.setFromId(req.getFromId());
            deleteFriendPack.setToId(req.getToId());
            messageProducer.sendToUser(
                    req.getFromId(),
                    req.getClientType(),
                    req.getImei(),
                    FriendshipEventCommand.FRIEND_DELETE,
                    deleteFriendPack,
                    req.getAppId()
            );

            //之后回调
            if (appConfig.isAddFriendAfterCallback()) {
                DeleteFriendAfterCallbackDto callbackDto = new DeleteFriendAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToId(req.getToId());
                callbackService.beforeCallback(
                        req.getAppId(),
                        Constants.CallbackCommand.DeleteFriendAfter,
                        JSONObject.toJSONString(callbackDto));
            }

        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteAllFriend(DeleteFriendReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("status", FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

        DeleteAllFriendPack deleteFriendPack = new DeleteAllFriendPack();
        deleteFriendPack.setFromId(req.getFromId());
        messageProducer.sendToUser(
                req.getFromId(),
                req.getClientType(),
                req.getImei(),
                FriendshipEventCommand.FRIEND_ALL_DELETE,
                deleteFriendPack,
                req.getAppId()
        );

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        imFriendShipMapper.update(update, query);
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        return ResponseVO.successResponse(imFriendShipMapper.selectList(query));
    }

    @Override
    public ResponseVO getRelation(GetRelationReq req) {

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());

        ImFriendShipEntity entity = imFriendShipMapper.selectOne(query);
        if (entity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(entity);
    }

    @Override
    public ResponseVO checkBlck(CheckFriendShipReq req) {

        Map<String, Integer> toIdMap
                = req.getToIds().stream().collect(Collectors
                .toMap(Function.identity(), s -> 0));
        List<CheckFriendShipResp> result = new ArrayList<>();
        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            result = imFriendShipMapper.checkFriendShipBlack(req);
        } else {
            result = imFriendShipMapper.checkFriendShipBlackBoth(req);
        }

        Map<String, Integer> collect = result.stream()
                .collect(Collectors
                        .toMap(CheckFriendShipResp::getToId,
                                CheckFriendShipResp::getStatus));
        for (String toId : toIdMap.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setStatus(toIdMap.get(toId));
                result.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(result);
    }


    @Override
    public ResponseVO addBlack(AddFriendShipBlackReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());

        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if (fromItem == null) {
            //走添加逻辑。
            fromItem = new ImFriendShipEntity();
            fromItem.setFromId(req.getFromId());
            fromItem.setToId(req.getToId());
            fromItem.setAppId(req.getAppId());
            fromItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }

        } else {
            //如果存在则判断状态，如果是拉黑，则提示已拉黑，如果是未拉黑，则修改状态
            if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
            } else {
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
                int result = imFriendShipMapper.update(update, query);
                if (result != 1) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_BLACK_ERROR);
                }
            }
        }

        AddFriendBlackPack addFriendBlackPack = new AddFriendBlackPack();
        addFriendBlackPack.setFromId(req.getFromId());
        addFriendBlackPack.setToId(req.getToId());
        //发送tcp通知
        messageProducer.sendToUser(
                req.getFromId(),
                req.getClientType(),
                req.getImei(),
                FriendshipEventCommand.FRIEND_BLACK_ADD,
                addFriendBlackPack,
                req.getAppId()
        );

        //之后回调
        if (appConfig.isAddFriendShipBlackAfterCallback()) {
            AddFriendBlackAfterCallbackDto callbackDto = new AddFriendBlackAfterCallbackDto();
            callbackDto.setFromId(req.getFromId());
            callbackDto.setToId(req.getToId());
            callbackService.beforeCallback(
                    req.getAppId(),
                    Constants.CallbackCommand.AddBlackAfter,
                    JSONObject.toJSONString(callbackDto)
            );
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteBlack(DeleteBlackReq req) {
        QueryWrapper queryFrom = new QueryWrapper<>()
                .eq("from_id", req.getFromId())
                .eq("app_id", req.getAppId())
                .eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);
        if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_NOT_YOUR_BLACK);
        }

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        int update1 = imFriendShipMapper.update(update, queryFrom);
        if (update1 == 1) {

            DeleteBlackPack deleteFriendPack = new DeleteBlackPack();
            deleteFriendPack.setFromId(req.getFromId());
            deleteFriendPack.setToId(req.getToId());
            messageProducer.sendToUser(
                    req.getFromId(),
                    req.getClientType(),
                    req.getImei(),
                    FriendshipEventCommand.FRIEND_BLACK_DELETE,
                    deleteFriendPack,
                    req.getAppId()
            );

            //之后回调
            if (appConfig.isAddFriendShipBlackAfterCallback()) {
                AddFriendBlackAfterCallbackDto callbackDto = new AddFriendBlackAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToId(req.getToId());
                callbackService.beforeCallback(
                        req.getAppId(),
                        Constants.CallbackCommand.DeleteBlack,
                        JSONObject.toJSONString(callbackDto)
                );
            }
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO checkFriendship(CheckFriendShipReq req) {

        Map<String, Integer> result
                = req.getToIds().stream()
                .collect(Collectors.toMap(Function.identity(), s -> 0));

        List<CheckFriendShipResp> resp = new ArrayList<>();

        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            resp = imFriendShipMapper.checkFriendShip(req);
        } else {
            resp = imFriendShipMapper.checkFriendShipBoth(req);
        }

        Map<String, Integer> collect = resp.stream()
                .collect(Collectors.toMap(CheckFriendShipResp::getToId
                        , CheckFriendShipResp::getStatus));

        for (String toId : result.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setStatus(result.get(toId));
                resp.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(resp);
    }

}
