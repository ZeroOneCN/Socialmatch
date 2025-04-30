-- 修复聊天消息表结构，使其与实体类匹配
USE soical001;

-- 检查当前表结构
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'soical001' 
AND TABLE_NAME = 't_chat_message';

-- 1. 添加receiver_id字段（如果不存在）
SET @sql = (SELECT IF(
    EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = 'soical001' 
        AND TABLE_NAME = 't_chat_message' 
        AND COLUMN_NAME = 'receiver_id'
    ),
    'SELECT "接收者字段已存在，无需添加" as message',
    'ALTER TABLE t_chat_message ADD COLUMN receiver_id BIGINT NOT NULL COMMENT "接收者ID" AFTER sender_id'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 添加外键约束（如果不存在）
SET @fkExistsQuery = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = 'soical001' 
    AND TABLE_NAME = 't_chat_message' 
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    AND CONSTRAINT_NAME = 'fk_receiver_id'
);

SET @addFkSql = CONCAT(
    'SELECT IF(', @fkExistsQuery, ' > 0, ',
    '"外键约束已存在", ',
    '"ALTER TABLE t_chat_message ADD CONSTRAINT fk_receiver_id FOREIGN KEY (receiver_id) REFERENCES t_user(user_id)")'
);

PREPARE stmt FROM @addFkSql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 修改type列为message_type（如果type存在且message_type不存在）
SET @sql = (SELECT IF(
    EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = 'soical001' 
        AND TABLE_NAME = 't_chat_message' 
        AND COLUMN_NAME = 'type'
    ) AND NOT EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = 'soical001' 
        AND TABLE_NAME = 't_chat_message' 
        AND COLUMN_NAME = 'message_type'
    ),
    'ALTER TABLE t_chat_message CHANGE COLUMN type message_type INT DEFAULT 1 COMMENT "消息类型：1-文本，2-图片"',
    'SELECT "无需修改type列" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 修改status列为is_read（如果status存在且is_read不存在）
SET @sql = (SELECT IF(
    EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = 'soical001' 
        AND TABLE_NAME = 't_chat_message' 
        AND COLUMN_NAME = 'status'
    ) AND NOT EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = 'soical001' 
        AND TABLE_NAME = 't_chat_message' 
        AND COLUMN_NAME = 'is_read'
    ),
    'ALTER TABLE t_chat_message CHANGE COLUMN status is_read BOOLEAN DEFAULT FALSE COMMENT "是否已读"',
    'SELECT "无需修改status列" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 更新现有记录，设置缺失的receiver_id值（可以基于会话设置）
UPDATE t_chat_message m
JOIN t_conversation c ON m.conversation_id = c.conversation_id
SET m.receiver_id = 
    CASE 
        WHEN m.sender_id = c.user_a_id THEN c.user_b_id
        ELSE c.user_a_id
    END
WHERE m.receiver_id = 0 OR m.receiver_id IS NULL;

-- 输出修复后的表结构
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'soical001' 
AND TABLE_NAME = 't_chat_message'
ORDER BY ORDINAL_POSITION; 