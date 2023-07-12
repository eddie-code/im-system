package com.learn.im.service.user.service;

import com.learn.im.common.ResponseVO;
import com.learn.im.service.user.dao.ImUserDataEntity;
import com.learn.im.service.user.model.req.*;
import com.learn.im.service.user.model.resp.GetUserInfoResp;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
public interface ImUserService {

    ResponseVO importUser(ImportUserReq req);

    ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId);

    ResponseVO deleteUser(DeleteUserReq req);

    ResponseVO modifyUserInfo(ModifyUserInfoReq req);


}
