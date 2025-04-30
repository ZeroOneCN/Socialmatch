-- 创建数据库
CREATE DATABASE IF NOT EXISTS soical001 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE soical001;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS t_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    avatar VARCHAR(255) DEFAULT '' COMMENT '头像URL',
    gender INT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    birthday DATE DEFAULT NULL COMMENT '生日',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 用户资料表
CREATE TABLE IF NOT EXISTS t_user_profile (
    profile_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    education INT DEFAULT 0 COMMENT '学历：0-未知，1-大专，2-本科，3-硕士，4-博士',
    occupation VARCHAR(50) DEFAULT '' COMMENT '职业',
    location VARCHAR(100) DEFAULT '' COMMENT '所在地',
    self_intro TEXT DEFAULT NULL COMMENT '自我介绍',
    hobbies TEXT DEFAULT NULL COMMENT '兴趣爱好',
    photos JSON DEFAULT NULL COMMENT '个人相册',
    avatar VARCHAR(255) DEFAULT '' COMMENT '头像URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES t_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料表';

-- 3. 动态表
CREATE TABLE IF NOT EXISTS t_post (
    post_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '内容',
    images JSON DEFAULT NULL COMMENT '图片列表',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    share_count INT DEFAULT 0 COMMENT '分享数',
    is_shared BOOLEAN DEFAULT FALSE COMMENT '是否为分享的动态',
    original_post_id BIGINT DEFAULT NULL COMMENT '原始动态ID（如果是分享的动态）',
    status INT DEFAULT 1 COMMENT '状态：0-删除，1-正常，2-违规',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    review_reason VARCHAR(255) DEFAULT NULL COMMENT '审核原因',
    review_time DATETIME DEFAULT NULL COMMENT '审核时间',
    FOREIGN KEY (user_id) REFERENCES t_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';


-- 4. 动态点赞表
CREATE TABLE IF NOT EXISTS t_post_like (
    like_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES t_post(post_id),
    FOREIGN KEY (user_id) REFERENCES t_user(user_id),
    UNIQUE KEY uk_post_user (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞表'; 


-- 5. 评论表
CREATE TABLE IF NOT EXISTS t_comment (
    comment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    status INT DEFAULT 1 COMMENT '状态：0-删除，1-正常，2-违规',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID，如果是回复评论则指向父评论，否则为null' AFTER status,
    reply_user_id BIGINT DEFAULT NULL COMMENT '被回复用户ID，如果是回复评论则为被回复用户ID，否则为null' AFTER parent_id,
    review_reason VARCHAR(255) DEFAULT NULL COMMENT '审核原因',
    review_time DATETIME DEFAULT NULL COMMENT '审核时间',
    FOREIGN KEY (post_id) REFERENCES t_post(post_id),
    FOREIGN KEY (user_id) REFERENCES t_user(user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_reply_user_id (reply_user_id),
    CONSTRAINT fk_parent_comment FOREIGN KEY (parent_id) REFERENCES t_comment(comment_id) ON DELETE SET NULL,
    CONSTRAINT fk_reply_user FOREIGN KEY (reply_user_id) REFERENCES t_user(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 6. 聊天会话表
CREATE TABLE IF NOT EXISTS t_conversation (
    conversation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_a_id BIGINT NOT NULL COMMENT '用户A',
    user_b_id BIGINT NOT NULL COMMENT '用户B',
    last_message TEXT DEFAULT NULL COMMENT '最后一条消息',
    last_message_time DATETIME DEFAULT NULL COMMENT '最后消息时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    unread_count INT DEFAULT 0 COMMENT '未读消息数量',
    FOREIGN KEY (user_a_id) REFERENCES t_user(user_id),
    FOREIGN KEY (user_b_id) REFERENCES t_user(user_id),
    UNIQUE KEY uk_users (user_a_id, user_b_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 7. 聊天消息表
CREATE TABLE IF NOT EXISTS t_chat_message (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type INT DEFAULT 1 COMMENT '消息类型：1-文本，2-图片',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (conversation_id) REFERENCES t_conversation(conversation_id),
    FOREIGN KEY (sender_id) REFERENCES t_user(user_id),
    FOREIGN KEY (receiver_id) REFERENCES t_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 8. 匹配记录表
CREATE TABLE IF NOT EXISTS t_user_match (
    match_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_a_id BIGINT NOT NULL COMMENT '用户A',
    user_b_id BIGINT NOT NULL COMMENT '用户B',
    status INT DEFAULT 0 COMMENT '状态：0-待确认，1-已匹配，2-已解除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_a_id) REFERENCES t_user(user_id),
    FOREIGN KEY (user_b_id) REFERENCES t_user(user_id),
    UNIQUE KEY uk_users (user_a_id, user_b_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匹配记录表';

-- 9. 系统配置表
CREATE TABLE IF NOT EXISTS t_system_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 10. 管理员表
CREATE TABLE IF NOT EXISTS t_admin (
    admin_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT '' COMMENT '头像',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 11. 操作日志表
CREATE TABLE IF NOT EXISTS t_operation_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_content TEXT COMMENT '操作内容',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES t_admin(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 创建用户关注表
CREATE TABLE IF NOT EXISTS t_user_follow (
    follow_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    follower_id BIGINT NOT NULL COMMENT '关注者ID（粉丝）',
    followed_id BIGINT NOT NULL COMMENT '被关注者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status INT DEFAULT 1 COMMENT '状态：0-已取消，1-已关注',
    FOREIGN KEY (follower_id) REFERENCES t_user(user_id),
    FOREIGN KEY (followed_id) REFERENCES t_user(user_id),
    UNIQUE KEY uk_follow (follower_id, followed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

- 城市表
CREATE TABLE IF NOT EXISTS t_city (
    city_id INT PRIMARY KEY AUTO_INCREMENT,
    province_name VARCHAR(50) NOT NULL COMMENT '省份名称',
    city_name VARCHAR(50) NOT NULL COMMENT '城市名称',
    city_code VARCHAR(20) NOT NULL COMMENT '城市编码',
    province_code VARCHAR(20) NOT NULL COMMENT '省份编码',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_city_code (city_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市信息表';

-- 职业表
CREATE TABLE IF NOT EXISTS t_occupation (
    occupation_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '职业名称',
    code VARCHAR(20) NOT NULL COMMENT '职业编码',
    category VARCHAR(50) DEFAULT NULL COMMENT '职业类别',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_occupation_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职业信息表';


-- 添加地区字段到用户资料表，用于同城功能
ALTER TABLE t_user_profile 
ADD COLUMN IF NOT EXISTS city VARCHAR(50) DEFAULT '' COMMENT '城市';

-- 添加分类字段到动态表，用于动态分类
ALTER TABLE t_post
ADD COLUMN IF NOT EXISTS post_type INT DEFAULT 0 COMMENT '动态类型：0-普通，1-图文，2-视频，3-链接';

-- 添加地区字段到动态表，用于同城功能（插入不了，提示错误）
ALTER TABLE t_post
ADD COLUMN IF NOT EXISTS city VARCHAR(50) DEFAULT '' COMMENT '发布城市';

-- 添加热度评分字段到动态表，用于推荐算法
ALTER TABLE t_post
ADD COLUMN IF NOT EXISTS hot_score DOUBLE DEFAULT 0 COMMENT '热度评分'; 


-- 插入初始管理员
INSERT INTO t_admin (username, password, nickname, status) 
VALUES ('admin', '$2a$10$ySG2lkvjFHY5O0./CPIE1OI8VJsuKYEzOYzqIa7AJR6sEgSzUFOAm', '超级管理员', 1); 

-- 创建用户互动表
CREATE TABLE `t_user_interaction` (
  `interaction_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '互动ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `target_id` bigint(20) NOT NULL COMMENT '目标ID（可能是用户ID、帖子ID等）',
  `type` tinyint(4) NOT NULL COMMENT '互动类型：1-喜欢用户，2-不喜欢用户，3-点赞帖子，4-评论帖子，5-分享帖子，6-查看资料，7-接触匹配',
  `source` varchar(50) DEFAULT NULL COMMENT '来源',
  `device_type` varchar(20) DEFAULT NULL COMMENT '设备类型',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`interaction_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户互动记录表';

-- 举报表
CREATE TABLE IF NOT EXISTS t_report (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_type INT NOT NULL COMMENT '举报类型：1-动态，2-评论，3-用户',
    reporter_id BIGINT NOT NULL COMMENT '举报人ID',
    target_id BIGINT NOT NULL COMMENT '被举报对象ID（动态ID、评论ID或用户ID）',
    reason VARCHAR(100) NOT NULL COMMENT '举报原因',
    content TEXT DEFAULT NULL COMMENT '举报内容描述',
    images JSON DEFAULT NULL COMMENT '证据图片',
    status INT DEFAULT 0 COMMENT '状态：0-待处理，1-已处理（违规），2-已处理（非违规），3-忽略',
    result VARCHAR(255) DEFAULT NULL COMMENT '处理结果说明',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    handler_id BIGINT DEFAULT NULL COMMENT '处理人ID（管理员ID）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (reporter_id) REFERENCES t_user(user_id),
    INDEX idx_target(report_type, target_id),
    INDEX idx_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表'; 

-- 创建用户同频偏好设置表
CREATE TABLE IF NOT EXISTS `t_user_preference` (
  `preference_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '偏好设置ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `interests` TEXT DEFAULT NULL COMMENT '兴趣偏好，多个以逗号分隔',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '意向城市',
  `city_code` VARCHAR(20) DEFAULT NULL COMMENT '城市编码',
  `nearby_only` BOOLEAN DEFAULT FALSE COMMENT '是否仅查看附近',
  `max_distance` INT DEFAULT 50 COMMENT '最大匹配距离(公里)',
  `min_age` INT DEFAULT 18 COMMENT '最小年龄',
  `max_age` INT DEFAULT 35 COMMENT '最大年龄',
  `education` INT DEFAULT 0 COMMENT '教育程度要求(0-不限，1-高中及以上，2-大专及以上，3-本科及以上，4-硕士及以上，5-博士及以上)',
  `occupation` VARCHAR(50) DEFAULT NULL COMMENT '职业意向',
  `occupation_code` VARCHAR(20) DEFAULT NULL COMMENT '职业编码',
  `verified_only` BOOLEAN DEFAULT FALSE COMMENT '是否仅显示已认证用户',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`preference_id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_user_preference_user_id` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户同频偏好设置表'; 


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
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统设置表';

- 插入AI助手相关的默认配置
INSERT INTO `t_system_settings` (`group`, `setting_key`, `setting_value`, `description`) VALUES
('ai_assistant', 'enabled', 'true', 'AI助手功能是否启用'),
('ai_assistant', 'name', 'Soical AI', 'AI助手的名称'),
('ai_assistant', 'avatar', '/static/images/ai_avatar.png', 'AI助手的头像URL'),
('ai_assistant', 'welcome_message', '你好！我是你的AI助手，有什么我可以帮你的吗？', 'AI助手的欢迎语'),
('ai_assistant', 'api_provider', 'deepseek', 'AI服务提供商'),
('ai_assistant', 'api_key', '', 'DeepSeek API密钥'),
('ai_assistant', 'api_base_url', 'https://api.deepseek.com/v1', 'DeepSeek API基础URL'),
('ai_assistant', 'model_name', 'deepseek-chat', 'DeepSeek模型名称'),
('ai_assistant', 'temperature', '0.7', 'AI回复的随机性程度'),
('ai_assistant', 'max_tokens', '2000', '单次回复的最大token数'),
('ai_assistant', 'system_prompt', '你是一个友好的AI助手，你的名字是Soical AI。你会以简洁、友好的方式回答用户的问题。', 'AI助手的系统提示词');

-- 创建AI助手会话记录表
CREATE TABLE IF NOT EXISTS `t_ai_chat` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI助手会话记录表';
