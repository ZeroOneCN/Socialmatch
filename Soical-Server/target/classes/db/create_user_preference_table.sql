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