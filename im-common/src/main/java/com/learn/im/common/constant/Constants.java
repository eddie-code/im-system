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

    /**
     * 读取时间 (心跳)
     */
    public static final String ReadTime = "readTime";

    public static class RedisConstants {

        /**
         * 用户session，appId + UserSessionConstants + 用户id 例如10000:userSession:lee
         */
        public static final String UserSessionConstants = ":userSession:";

    }

    public static class RabbitConstants{

        /**
         * im服务给用户服务投递的消息
         */
        public static final String Im2UserService = "pipeline2UserService";
        /**
         * im服务给消息服务投递的消息
         */
        public static final String Im2MessageService = "pipeline2MessageService";
        /**
         *
         */
        public static final String Im2GroupService = "pipeline2GroupService";
        /**
         *
         */
        public static final String Im2FriendshipService = "pipeline2FriendshipService";
        /**
         * 消息服务给im服务投递的消息
         */
        public static final String MessageService2Im = "messageService2Pipeline";
        /**
         * 群组服务给im服务投递的消息
         */
        public static final String GroupService2Im = "GroupService2Pipeline";
        /**
         *
         */
        public static final String FriendShip2Im = "friendShip2Pipeline";

    }

}
