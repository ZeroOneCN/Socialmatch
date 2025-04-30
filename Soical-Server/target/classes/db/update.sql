-- 添加未读消息计数列到会话表
ALTER TABLE t_conversation ADD COLUMN unread_count INT DEFAULT 0 COMMENT '未读消息数量';

-- 更新已有会话的未读计数为0
UPDATE t_conversation SET unread_count = 0 WHERE unread_count IS NULL;

-- 更新数据库表结构

USE soical001;

-- 1. 添加t_post表中缺失的字段
ALTER TABLE t_post 
ADD COLUMN review_reason VARCHAR(255) DEFAULT NULL COMMENT '审核原因' AFTER status,
ADD COLUMN review_time DATETIME DEFAULT NULL COMMENT '审核时间' AFTER review_reason,
ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

-- 2. 创建举报表t_report
CREATE TABLE t_report (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '举报用户ID',
    report_type INT NOT NULL COMMENT '举报类型：1-帖子，2-评论，3-用户',
    target_id BIGINT NOT NULL COMMENT '被举报内容ID（根据举报类型对应不同表的ID）',
    reason VARCHAR(255) NOT NULL COMMENT '举报原因',
    screenshots TEXT DEFAULT NULL COMMENT '截图证据（JSON数组）',
    status INT DEFAULT 0 COMMENT '举报状态：0-待处理，1-已处理有效，2-已处理无效',
    result VARCHAR(255) DEFAULT NULL COMMENT '处理结果',
    handler_id BIGINT DEFAULT NULL COMMENT '处理人ID',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES t_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 3. 添加缺失的t_post表的列
ALTER TABLE t_post 
ADD COLUMN hot_score DOUBLE DEFAULT 0 COMMENT '热度评分' AFTER post_type;

-- 4. 添加t_post表中的city和post_type字段（如果不存在）
ALTER TABLE t_post
ADD COLUMN city VARCHAR(50) DEFAULT '' COMMENT '发布城市' AFTER original_post_id,
ADD COLUMN post_type INT DEFAULT 0 COMMENT '动态类型：0-普通，1-图文，2-视频，3-链接' AFTER city; 

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