package com.learn.im.service.user.model.req;

import com.learn.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@Data
public class GetUserSequenceReq extends RequestBase {

    private String userId;

}
