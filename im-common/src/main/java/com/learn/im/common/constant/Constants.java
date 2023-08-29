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
     * 区分设备的
     */
    public static final String Imei = "imei";

    /**
     * 读取时间 (心跳)
     */
    public static final String ReadTime = "readTime";

    /**
     *
     */
    public static final String ImCoreZkRoot = "/im-coreRoot";
    /**
     *
     */
    public static final String ImCoreZkRootTcp = "/tcp";
    /**
     *
     */
    public static final String ImCoreZkRootWeb = "/web";

    public static class RedisConstants {

        /**
         * 用户session，appId + UserSessionConstants + 用户id 例如10000:userSession:lee
         */
        public static final String UserSessionConstants = ":userSession:";

        /**
         * 用户上线通知channel
         */
        public static final String UserLoginChannel = "signal/channel/LOGIN_USER_INNER_QUEUE";

    }

    public static class RabbitConstants {

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

    public static class CallbackCommand {

        public static final String ModifyUserAfter = "user.modify.after";

        public static final String CreateGroupAfter = "group.create.after";

        public static final String UpdateGroupAfter = "group.update.after";

        public static final String DestoryGroupAfter = "group.destory.after";

        public static final String TransferGroupAfter = "group.transfer.after";

        public static final String GroupMemberAddBefore = "group.member.add.before";

        public static final String GroupMemberAddAfter = "group.member.add.after";

        public static final String GroupMemberDeleteAfter = "group.member.delete.after";

        public static final String AddFriendBefore = "friend.add.before";

        public static final String AddFriendAfter = "friend.add.after";

        public static final String UpdateFriendBefore = "friend.update.before";

        public static final String UpdateFriendAfter = "friend.update.after";

        public static final String DeleteFriendAfter = "friend.delete.after";

        public static final String AddBlackAfter = "black.add.after";

        public static final String DeleteBlack = "black.delete";

        public static final String SendMessageAfter = "message.send.after";

        public static final String SendMessageBefore = "message.send.before";

    }
}
