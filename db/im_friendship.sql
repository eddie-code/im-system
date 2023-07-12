CREATE TABLE `im_friendship`
(
    `app_id`          int                                                          NOT NULL COMMENT 'app_id',
    `from_id`         varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'from_id',
    `to_id`           varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'to_id',
    `remark`          varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci   DEFAULT NULL COMMENT '备注',
    `status`          int                                                            DEFAULT NULL COMMENT '状态 1正常 2删除',
    `black`           int                                                            DEFAULT NULL COMMENT '1正常 2拉黑',
    `create_time`     bigint                                                         DEFAULT NULL,
    `friend_sequence` bigint                                                         DEFAULT NULL,
    `black_sequence`  bigint                                                         DEFAULT NULL,
    `add_source`      varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci   DEFAULT NULL COMMENT '来源',
    `extra`           varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '来源',
    PRIMARY KEY (`app_id`, `from_id`, `to_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  ROW_FORMAT = DYNAMIC;