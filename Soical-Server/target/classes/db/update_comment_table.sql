-- 添加父评论ID和被回复用户ID字段到评论表
ALTER TABLE t_comment
ADD COLUMN parent_id BIGINT DEFAULT NULL COMMENT '父评论ID，如果是回复评论则指向父评论，否则为null' AFTER status;

ALTER TABLE t_comment
ADD COLUMN reply_user_id BIGINT DEFAULT NULL COMMENT '被回复用户ID，如果是回复评论则为被回复用户ID，否则为null' AFTER parent_id;

-- 添加索引，提高查询效率
ALTER TABLE t_comment
ADD INDEX idx_parent_id (parent_id),
ADD INDEX idx_reply_user_id (reply_user_id);

-- 添加外键约束（可选，如果您希望严格保证数据一致性）
ALTER TABLE t_comment
ADD CONSTRAINT fk_parent_comment FOREIGN KEY (parent_id) REFERENCES t_comment(comment_id) ON DELETE SET NULL,
ADD CONSTRAINT fk_reply_user FOREIGN KEY (reply_user_id) REFERENCES t_user(user_id) ON DELETE SET NULL; 