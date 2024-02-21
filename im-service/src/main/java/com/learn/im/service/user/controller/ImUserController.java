package com.learn.im.service.user.controller;

import com.learn.im.common.ClientType;
import com.learn.im.common.ResponseVO;
import com.learn.im.common.route.RouteHandle;
import com.learn.im.common.route.RouteInfo;
import com.learn.im.common.utils.RouteInfoParseUtil;
import com.learn.im.service.user.model.req.*;
import com.learn.im.service.user.service.ImUserService;
import com.learn.im.service.utils.ZKit;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/user")
public class ImUserController {

    private final ImUserService imUserService;

    private final RouteHandle routeHandle;

    private final ZKit zKit;

    @PostMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req, Integer appId) {
        return imUserService.importUser(req);
    }

    @PostMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }

    /**
     * im的登录接口，返回im地址
     */
    @RequestMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req, Integer appId) {
        req.setAppId(appId);

        ResponseVO login = imUserService.login(req);
        if (login.isOk()) {
            // TODO 去ZK获取一个im的地址, 返回给sdk
            List<String> allNode;
            if (req.getClientType() == ClientType.WEB.getCode()) {
                allNode = zKit.getAllWebNode();
            } else {
                allNode = zKit.getAllTcpNode();
            }
            String s = routeHandle.routeServer(allNode, req.getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(s);
            return ResponseVO.successResponse(parse);
        }

        return ResponseVO.errorResponse();
    }

    /**
     * 客户端在登录的时候， 调用此接口， 判断本地的 Seq 和服务端的 Seq 是否一致。
     * 如果一致的话， 那么就不需要进行数据同步， 如果不一致则调用相应的接口， 进行数据同步
     */
    @RequestMapping("/getUserSequence")
    public ResponseVO getUserSequence(@RequestBody @Validated GetUserSequenceReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.getUserSequence(req);
    }

}
