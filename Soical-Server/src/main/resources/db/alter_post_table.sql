-- 添加分享相关字段到t_post表
ALTER TABLE t_post ADD COLUMN share_count INT DEFAULT 0 COMMENT '分享数';
ALTER TABLE t_post ADD COLUMN is_shared BOOLEAN DEFAULT FALSE COMMENT '是否为分享的动态';
ALTER TABLE t_post ADD COLUMN original_post_id BIGINT DEFAULT NULL COMMENT '原始动态ID（如果是分享的动态）'; 