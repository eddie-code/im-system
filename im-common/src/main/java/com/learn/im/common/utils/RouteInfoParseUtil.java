package com.learn.im.common.utils;


import com.learn.im.common.BaseErrorCode;
import com.learn.im.common.exception.ApplicationException;
import com.learn.im.common.route.RouteInfo;

/**
 *
 * @author lee
 */
public class RouteInfoParseUtil {

    public static RouteInfo parse(String info){
        try {
            String[] serverInfo = info.split(":");
            RouteInfo routeInfo =  new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1])) ;
            return routeInfo ;
        }catch (Exception e){
            throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR) ;
        }
    }
}
