package com.learn.im.service.user.model.req;

import com.learn.im.common.model.RequestBase;
import com.learn.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;


@Data
public class ImportUserReq extends RequestBase {

    private List<ImUserDataEntity> userData;


}
