-- 向t_user_profile表添加avatar字段
ALTER TABLE t_user_profile ADD COLUMN avatar VARCHAR(255) DEFAULT '' COMMENT '头像URL';

-- 从用户表同步头像数据到用户资料表
UPDATE t_user_profile p 
JOIN t_user u ON p.user_id = u.user_id 
SET p.avatar = u.avatar 
WHERE u.avatar IS NOT NULL; 