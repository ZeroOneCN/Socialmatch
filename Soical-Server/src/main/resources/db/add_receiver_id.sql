-- 添加接收者ID列到聊天消息表
ALTER TABLE t_chat_message ADD COLUMN receiver_id BIGINT COMMENT '接收者ID';

-- 添加外键约束
ALTER TABLE t_chat_message ADD CONSTRAINT fk_receiver_id FOREIGN KEY (receiver_id) REFERENCES t_user(user_id);

-- 更新消息类型字段名（如果需要）
ALTER TABLE t_chat_message CHANGE COLUMN type message_type INT DEFAULT 1 COMMENT '消息类型：1-文本，2-图片';

-- 更新消息状态字段名（如果需要）
ALTER TABLE t_chat_message CHANGE COLUMN status is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读：0-未读，1-已读'; 