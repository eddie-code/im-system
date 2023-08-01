package com.learn.im.common.constant;

/**
 * @author lee
 * @description
 */
public class Constants {

    /**
     * channel绑定的userId Key
     */
    public static final String UserId = "userId";

    /**
     * channel绑定的appId
     */
    public static final String AppId = "appId";

    /**
     * 端类型
     */
    public static final String ClientType = "clientType";

    public static class RedisConstants {

        /**
         * 用户session，appId + UserSessionConstants + 用户id 例如10000:userSession:lee
         */
        public static final String UserSessionConstants = ":userSession:";

    }
}
