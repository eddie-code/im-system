package com.learn.im.common.route.algorithm.random;

import com.learn.im.common.enums.UserErrorCode;
import com.learn.im.common.exception.ApplicationException;
import com.learn.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @description:
 * @author: lee
 * @version: 1.0
 */
public class RandomHandle implements RouteHandle {

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if(size == 0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        // 小于 size 的一个数字
        int i = ThreadLocalRandom.current().nextInt(size);
        return values.get(i);
    }
}
