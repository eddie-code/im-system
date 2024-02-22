package com.learn.im.codec.pack.user;

import com.learn.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * @author lee
 * @description
 */
@Data
public class UserStatusChangeNotifyPack {

    private Integer appId;

    private String userId;

    private Integer status;

    private List<UserSession> client;

}
