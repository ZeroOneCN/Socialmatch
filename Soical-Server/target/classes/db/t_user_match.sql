CREATE TABLE `t_user_match` (
  `match_id` bigint NOT NULL AUTO_INCREMENT,
  `user_a_id` bigint NOT NULL COMMENT '用户A',
  `user_b_id` bigint NOT NULL COMMENT '用户B',
  `status` int DEFAULT '0' COMMENT '状态：0-待确认，1-已匹配，2-已解除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `first_message` text COMMENT '单向首条消息内容',
  `first_message_time` datetime DEFAULT NULL COMMENT '首条消息发送时间',
  `first_message_read` tinyint(1) DEFAULT '0' COMMENT '首条消息是否已读',
  PRIMARY KEY (`match_id`),
  UNIQUE KEY `uk_users` (`user_a_id`,`user_b_id`),
  KEY `user_b_id` (`user_b_id`),
  CONSTRAINT `t_user_match_ibfk_1` FOREIGN KEY (`user_a_id`) REFERENCES `t_user` (`user_id`),
  CONSTRAINT `t_user_match_ibfk_2` FOREIGN KEY (`user_b_id`) REFERENCES `t_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='匹配记录表';
