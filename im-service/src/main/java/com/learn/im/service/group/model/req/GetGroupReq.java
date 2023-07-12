package com.learn.im.service.group.model.req;

import com.learn.im.common.model.RequestBase;
import lombok.Data;

/**
 * @author: lee
 * @description:
 **/
@Data
public class GetGroupReq extends RequestBase {

    private String groupId;

}
