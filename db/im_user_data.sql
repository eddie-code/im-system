CREATE TABLE `im_user_data`
(
    `user_id`            varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
    `app_id`             int                                                          NOT NULL,
    `nick_name`          varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         DEFAULT NULL COMMENT '昵称',
    `password`           varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         DEFAULT NULL,
    `photo`              varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         DEFAULT NULL,
    `user_sex`           int                                                                   DEFAULT NULL,
    `birth_day`          varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci          DEFAULT NULL COMMENT '生日',
    `location`           varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci          DEFAULT NULL COMMENT '地址',
    `self_signature`     varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         DEFAULT NULL COMMENT '个性签名',
    `friend_allow_type`  int                                                          NOT NULL DEFAULT '1' COMMENT '加好友验证类型（Friend_AllowType） 1无需验证 2需要验证',
    `forbidden_flag`     int                                                          NOT NULL DEFAULT '0' COMMENT '禁用标识 1禁用',
    `disable_add_friend` int                                                          NOT NULL DEFAULT '0' COMMENT '管理员禁止用户添加加好友：0 未禁用 1 已禁用',
    `silent_flag`        int                                                          NOT NULL DEFAULT '0' COMMENT '禁言标识 1禁言',
    `user_type`          int                                                          NOT NULL DEFAULT '1' COMMENT '用户类型 1普通用户 2客服 3机器人',
    `del_flag`           int                                                          NOT NULL DEFAULT '0',
    `extra`              varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci        DEFAULT NULL,
    PRIMARY KEY (`app_id`, `user_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  ROW_FORMAT = DYNAMIC;