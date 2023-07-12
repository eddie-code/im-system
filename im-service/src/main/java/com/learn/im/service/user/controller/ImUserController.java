package com.learn.im.service.user.controller;

import com.learn.im.common.ResponseVO;
import com.learn.im.service.user.model.req.*;
import com.learn.im.service.user.service.ImUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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

    @PostMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req, Integer appId) {
        return imUserService.importUser(req);
    }

    @PostMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }

}
