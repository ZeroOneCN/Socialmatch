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

-- 添加地区字段到用户资料表，用于同城功能
ALTER TABLE t_user_profile 
ADD COLUMN IF NOT EXISTS city VARCHAR(50) DEFAULT '' COMMENT '城市';

-- 添加分类字段到动态表，用于动态分类
ALTER TABLE t_post
ADD COLUMN IF NOT EXISTS post_type INT DEFAULT 0 COMMENT '动态类型：0-普通，1-图文，2-视频，3-链接';

-- 添加地区字段到动态表，用于同城功能
ALTER TABLE t_post
ADD COLUMN IF NOT EXISTS city VARCHAR(50) DEFAULT '' COMMENT '发布城市';

-- 添加热度评分字段到动态表，用于推荐算法
ALTER TABLE t_post
ADD COLUMN IF NOT EXISTS hot_score DOUBLE DEFAULT 0 COMMENT '热度评分'; 