package com.learn.im.service.user.model.req;

import com.learn.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * @author lee
 * @description
 */
@Data
public class SubscribeUserOnlineStatusReq extends RequestBase {

    private List<String> subUserId;

    private Long subTime;

}
