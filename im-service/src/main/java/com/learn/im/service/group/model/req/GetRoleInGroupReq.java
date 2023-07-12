package com.learn.im.service.group.model.req;

import com.learn.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * @author: lee
 * @description:
 **/
@Data
public class GetRoleInGroupReq extends RequestBase {

    private String groupId;

    private List<String> memberId;
}
