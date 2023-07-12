package com.learn.im.service.user.model.resp;

import com.learn.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@Data
public class UserOnlineStatusResp {

    private List<UserSession> session;

    private String customText;

    private Integer customStatus;

}
