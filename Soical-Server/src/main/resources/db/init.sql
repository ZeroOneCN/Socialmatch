-- t_admin definition

CREATE DATABASE `Social`;
USE `Social`;

CREATE TABLE `t_admin` (
  `admin_id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT '' COMMENT '昵称',
  `avatar` varchar(255) DEFAULT '' COMMENT '头像',
  `status` int DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员表';

LOCK TABLES `t_admin` WRITE;
INSERT INTO `t_admin` VALUES (1,'admin','$2a$10$sa.LDswPwZGaZlf1YZ9RHOKE3pjU85Cp4RSfdELYjpc0l1vZF3oNK','超级管理员','https://img2.baidu.com/it/u=4080406301,2440225087&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=651',1,'2025-04-07 00:05:02','2025-04-08 18:20:43');
UNLOCK TABLES;

CREATE TABLE `t_base_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT,
  `config_type` varchar(50) NOT NULL COMMENT '配置类型：HOBBY-兴趣爱好, EDUCATION-学历要求, AGE_RANGE-年龄范围, LOCATION-地区',
  `config_key` varchar(50) NOT NULL COMMENT '配置键',
  `config_value` varchar(100) NOT NULL COMMENT '配置值',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `parent_id` bigint DEFAULT NULL COMMENT '父级ID（用于地区等层级数据）',
  `status` int DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_type_key` (`config_type`,`config_key`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_type` (`config_type`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础数据配置表';


-- t_city definition

CREATE TABLE `t_city` (
  `city_id` int NOT NULL AUTO_INCREMENT,
  `province_name` varchar(50) NOT NULL COMMENT '省份名称',
  `city_name` varchar(50) NOT NULL COMMENT '城市名称',
  `city_code` varchar(20) NOT NULL COMMENT '城市编码',
  `province_code` varchar(20) NOT NULL COMMENT '省份编码',
  `status` int DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`city_id`),
  UNIQUE KEY `uk_city_code` (`city_code`)
) ENGINE=InnoDB AUTO_INCREMENT=345 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='城市信息表';


-- t_education_verification definition

CREATE TABLE `t_education_verification` (
  `verification_id` bigint NOT NULL COMMENT '认证ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '认证类型：education-教育认证',
  `status` varchar(20) NOT NULL COMMENT '认证状态：pending-待审核，approved-已通过，rejected-已拒绝',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NOT NULL COMMENT '认证提交时间',
  `update_time` datetime DEFAULT NULL COMMENT '认证审核时间',
  `school` varchar(100) NOT NULL COMMENT '学校名称',
  `college` varchar(100) DEFAULT NULL COMMENT '学院',
  `major` varchar(100) NOT NULL COMMENT '专业',
  `student_id` varchar(50) NOT NULL COMMENT '学号',
  `enrollment_year` int NOT NULL COMMENT '入学年份',
  `student_card_front` varchar(255) NOT NULL COMMENT '学生证封面照片URL',
  `student_card_inside` varchar(255) NOT NULL COMMENT '学生证内页照片URL',
  PRIMARY KEY (`verification_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教育认证表';


-- t_identity_verification definition

CREATE TABLE `t_identity_verification` (
  `verification_id` bigint NOT NULL COMMENT '认证ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '认证类型：identity-身份认证',
  `status` varchar(20) NOT NULL COMMENT '认证状态：pending-待审核，approved-已通过，rejected-已拒绝',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NOT NULL COMMENT '认证提交时间',
  `update_time` datetime DEFAULT NULL COMMENT '认证审核时间',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `id_number` varchar(18) NOT NULL COMMENT '身份证号',
  `id_card_front` varchar(255) NOT NULL COMMENT '身份证正面照片URL',
  `id_card_back` varchar(255) NOT NULL COMMENT '身份证背面照片URL',
  `face_image` varchar(255) DEFAULT NULL COMMENT '人脸',
  PRIMARY KEY (`verification_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='身份认证表';


-- t_system_settings definition

CREATE TABLE `t_system_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group` varchar(50) NOT NULL COMMENT '设置组（如security, basic, notification等）',
  `setting_key` varchar(50) NOT NULL COMMENT '设置键',
  `setting_value` text COMMENT '设置值（可能是字符串、数字、JSON等）',
  `description` varchar(255) DEFAULT NULL COMMENT '设置描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_key` (`group`,`setting_key`) COMMENT '组和键的唯一索引'
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统设置表';


-- t_user definition

CREATE TABLE `t_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `avatar` varchar(255) DEFAULT '' COMMENT '头像URL',
  `gender` int DEFAULT '0' COMMENT '性别：0-未知，1-男，2-女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `status` int DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';


-- t_user_interaction definition

CREATE TABLE `t_user_interaction` (
  `interaction_id` bigint NOT NULL AUTO_INCREMENT COMMENT '互动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID（可能是用户ID、帖子ID等）',
  `type` tinyint NOT NULL COMMENT '互动类型：1-喜欢用户，2-不喜欢用户，3-点赞帖子，4-评论帖子，5-分享帖子，6-查看资料，7-接触匹配',
  `source` varchar(50) DEFAULT NULL COMMENT '来源',
  `device_type` varchar(20) DEFAULT NULL COMMENT '设备类型',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`interaction_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户互动记录表';


-- t_verification definition

CREATE TABLE `t_verification` (
  `verification_id` bigint NOT NULL AUTO_INCREMENT COMMENT '认证ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '认证类型：identity-身份认证，education-教育认证',
  `status` varchar(20) NOT NULL COMMENT '认证状态：pending-待审核，approved-已通过，rejected-已拒绝',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NOT NULL COMMENT '认证提交时间',
  `update_time` datetime DEFAULT NULL COMMENT '认证审核时间',
  PRIMARY KEY (`verification_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type_status` (`type`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户认证表';


-- t_ai_chat definition

CREATE TABLE `t_ai_chat` (
  `chat_id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `message` text NOT NULL COMMENT '消息内容',
  `role` varchar(20) NOT NULL COMMENT '角色：user-用户, assistant-AI助手',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `tokens` int DEFAULT NULL COMMENT '消息的token数量',
  PRIMARY KEY (`chat_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_ai_chat_user_id` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI助手会话记录表';


-- t_conversation definition

CREATE TABLE `t_conversation` (
  `conversation_id` bigint NOT NULL AUTO_INCREMENT,
  `user_a_id` bigint NOT NULL COMMENT '用户A',
  `user_b_id` bigint NOT NULL COMMENT '用户B',
  `last_message` text COMMENT '最后一条消息',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `unread_count` int DEFAULT '0' COMMENT '未读消息数量',
  PRIMARY KEY (`conversation_id`),
  UNIQUE KEY `uk_users` (`user_a_id`,`user_b_id`),
  KEY `user_b_id` (`user_b_id`),
  CONSTRAINT `t_conversation_ibfk_1` FOREIGN KEY (`user_a_id`) REFERENCES `t_user` (`user_id`),
  CONSTRAINT `t_conversation_ibfk_2` FOREIGN KEY (`user_b_id`) REFERENCES `t_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天会话表';


-- t_post definition

CREATE TABLE `t_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` text NOT NULL COMMENT '内容',
  `images` json DEFAULT NULL COMMENT '图片列表',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `comment_count` int DEFAULT '0' COMMENT '评论数',
  `status` int DEFAULT '1' COMMENT '状态：0-删除，1-正常，2-违规',
  `review_reason` varchar(255) DEFAULT NULL COMMENT '审核原因',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `share_count` int DEFAULT '0' COMMENT '分享数',
  `is_shared` tinyint(1) DEFAULT '0' COMMENT '是否为分享的动态',
  `original_post_id` bigint DEFAULT NULL COMMENT '原始动态ID（如果是分享的动态）',
  `post_type` int DEFAULT '0' COMMENT '动态类型：0-普通，1-图文，2-视频，3-链接',
  `hot_score` double DEFAULT '0' COMMENT '热度评分',
  `city` varchar(50) DEFAULT '' COMMENT '发布城市',
  PRIMARY KEY (`post_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `t_post_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='动态表';


-- t_post_like definition

CREATE TABLE `t_post_like` (
  `like_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '动态ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`like_id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `t_post_like_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `t_post` (`post_id`),
  CONSTRAINT `t_post_like_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='动态点赞表';


-- t_report definition

CREATE TABLE `t_report` (
  `report_id` bigint NOT NULL AUTO_INCREMENT,
  `report_type` int NOT NULL COMMENT '举报类型：1-动态，2-评论，3-用户',
  `reporter_id` bigint NOT NULL COMMENT '举报人ID',
  `target_id` bigint NOT NULL COMMENT '被举报对象ID（动态ID、评论ID或用户ID）',
  `reason` varchar(100) NOT NULL COMMENT '举报原因',
  `content` text COMMENT '举报内容描述',
  `images` json DEFAULT NULL COMMENT '证据图片',
  `status` int DEFAULT '0' COMMENT '状态：0-待处理，1-已处理（违规），2-已处理（非违规），3-忽略',
  `result` varchar(255) DEFAULT NULL COMMENT '处理结果说明',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID（管理员ID）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`report_id`),
  KEY `reporter_id` (`reporter_id`),
  KEY `idx_target` (`report_type`,`target_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `t_report_ibfk_1` FOREIGN KEY (`reporter_id`) REFERENCES `t_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='举报表';


-- t_user_follow definition

CREATE TABLE `t_user_follow` (
  `follow_id` bigint NOT NULL AUTO_INCREMENT,
  `follower_id` bigint NOT NULL COMMENT '关注者ID（粉丝）',
  `followed_id` bigint NOT NULL COMMENT '被关注者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` int DEFAULT '1' COMMENT '状态：0-已取消，1-已关注',
  PRIMARY KEY (`follow_id`),
  UNIQUE KEY `uk_follow` (`follower_id`,`followed_id`),
  KEY `followed_id` (`followed_id`),
  CONSTRAINT `t_user_follow_ibfk_1` FOREIGN KEY (`follower_id`) REFERENCES `t_user` (`user_id`),
  CONSTRAINT `t_user_follow_ibfk_2` FOREIGN KEY (`followed_id`) REFERENCES `t_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注表';


-- t_user_match definition

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
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='匹配记录表';


-- t_user_preference definition

CREATE TABLE `t_user_preference` (
  `preference_id` bigint NOT NULL AUTO_INCREMENT COMMENT '偏好设置ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `interests` text COMMENT '兴趣偏好，多个以逗号分隔',
  `city` varchar(50) DEFAULT NULL COMMENT '意向城市',
  `city_code` varchar(20) DEFAULT NULL COMMENT '城市编码',
  `nearby_only` tinyint(1) DEFAULT '0' COMMENT '是否仅查看附近',
  `max_distance` int DEFAULT '50' COMMENT '最大匹配距离(公里)',
  `min_age` int DEFAULT '18' COMMENT '最小年龄',
  `max_age` int DEFAULT '35' COMMENT '最大年龄',
  `education` int DEFAULT '0' COMMENT '教育程度要求(0-不限，1-高中及以上，2-大专及以上，3-本科及以上，4-硕士及以上，5-博士及以上)',
  `occupation` varchar(50) DEFAULT NULL COMMENT '职业意向',
  `occupation_code` varchar(20) DEFAULT NULL COMMENT '职业编码',
  `verified_only` tinyint(1) DEFAULT '0' COMMENT '是否仅显示已认证用户',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `gender_preference` int DEFAULT NULL COMMENT '性别',
  PRIMARY KEY (`preference_id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_user_preference_user_id` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户同频偏好设置表';


-- t_user_profile definition

CREATE TABLE `t_user_profile` (
  `profile_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `nickname` varchar(50) DEFAULT '' COMMENT '昵称',
  `education` int DEFAULT '0' COMMENT '学历：0-未知，1-大专，2-本科，3-硕士，4-博士',
  `occupation` varchar(50) DEFAULT '' COMMENT '职业',
  `location` varchar(100) DEFAULT '' COMMENT '所在地',
  `self_intro` text COMMENT '自我介绍',
  `hobbies` text COMMENT '兴趣爱好',
  `photos` json DEFAULT NULL COMMENT '个人相册',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avatar` varchar(255) DEFAULT '' COMMENT '头像URL',
  `city` varchar(50) DEFAULT '' COMMENT '城市',
  `city_code` varchar(20) DEFAULT NULL COMMENT '城市代码',
  `province_code` varchar(20) DEFAULT NULL COMMENT '省份代码',
  `gender` int DEFAULT NULL COMMENT '性别：0-位置，1-男，2-女，3-保密',
  `birthday` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生日',
  `occupation_code` varchar(20) DEFAULT NULL COMMENT '职业代码',
  `personality` text COMMENT '性格特征',
  `values` text COMMENT '价值观',
  `lifestyle` text COMMENT '生活习惯',
  `activity_level` int DEFAULT NULL COMMENT '活跃度',
  `last_active_time` datetime DEFAULT NULL COMMENT '最后活跃时间',
  PRIMARY KEY (`profile_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `t_user_profile_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户资料表';


-- t_user_settings definition

CREATE TABLE `t_user_settings` (
  `setting_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receive_first_message` int DEFAULT '1' COMMENT '接收首条消息设置：0-拒绝所有，1-接收全部，2-仅接收推荐用户',
  `show_online_status` tinyint(1) DEFAULT '1' COMMENT '显示在线状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`setting_id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `t_user_settings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户设置表';


-- t_chat_message definition

CREATE TABLE `t_chat_message` (
  `message_id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `message_type` int DEFAULT '1' COMMENT '消息类型：1-文本，2-图片',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `receiver_id` bigint DEFAULT NULL COMMENT '接收者ID',
  `room_id` bigint DEFAULT NULL,
  `media_url` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`message_id`),
  KEY `conversation_id` (`conversation_id`),
  KEY `sender_id` (`sender_id`),
  KEY `fk_receiver_id` (`receiver_id`),
  CONSTRAINT `fk_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `t_user` (`user_id`),
  CONSTRAINT `t_chat_message_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `t_conversation` (`conversation_id`),
  CONSTRAINT `t_chat_message_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `t_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表';


-- t_comment definition

CREATE TABLE `t_comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '动态ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `status` int DEFAULT '1' COMMENT '状态：0-删除，1-正常，2-违规',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID，如果是回复评论则指向父评论，否则为null',
  `reply_user_id` bigint DEFAULT NULL COMMENT '被回复用户ID，如果是回复评论则为被回复用户ID，否则为null',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `review_reason` varchar(255) DEFAULT NULL COMMENT '审核原因',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`comment_id`),
  KEY `post_id` (`post_id`),
  KEY `user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_reply_user_id` (`reply_user_id`),
  CONSTRAINT `fk_parent_comment` FOREIGN KEY (`parent_id`) REFERENCES `t_comment` (`comment_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_reply_user` FOREIGN KEY (`reply_user_id`) REFERENCES `t_user` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `t_comment_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `t_post` (`post_id`),
  CONSTRAINT `t_comment_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';