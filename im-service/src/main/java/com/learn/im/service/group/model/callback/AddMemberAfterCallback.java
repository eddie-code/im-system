package com.learn.im.service.group.model.callback;

import com.learn.im.service.group.model.resp.AddMemberResp;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
@Data
public class AddMemberAfterCallback {

    private String groupId;

    private Integer groupType;

    private String operater;

    private List<AddMemberResp> memberId;
}
