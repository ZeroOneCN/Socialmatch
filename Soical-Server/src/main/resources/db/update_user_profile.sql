-- 向用户资料表添加必要的新字段
ALTER TABLE t_user_profile ADD COLUMN IF NOT EXISTS city_code VARCHAR(20) DEFAULT '' COMMENT '城市代码';
ALTER TABLE t_user_profile ADD COLUMN IF NOT EXISTS province_code VARCHAR(20) DEFAULT '' COMMENT '省份代码';
ALTER TABLE t_user_profile ADD COLUMN IF NOT EXISTS occupation_code VARCHAR(20) DEFAULT '' COMMENT '职业代码';
ALTER TABLE t_user_profile ADD COLUMN IF NOT EXISTS gender INT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女，3-保密';
ALTER TABLE t_user_profile ADD COLUMN IF NOT EXISTS birthday VARCHAR(20) DEFAULT '' COMMENT '生日';

-- 更新日志消息
SELECT 'User profile table updated with new fields: city_code, province_code, occupation_code, gender, birthday' AS message; 