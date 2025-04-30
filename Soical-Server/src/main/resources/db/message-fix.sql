-- 聊天消息表结构修复脚本
-- 创建于 2023-09-10
-- 修改聊天消息表字段名称和添加缺失字段

-- 检查并添加receiver_id字段
SET @column_exists = 0;
SELECT COUNT(*) INTO @column_exists FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 't_chat_message' AND COLUMN_NAME = 'receiver_id' AND TABLE_SCHEMA = DATABASE();

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE t_chat_message ADD COLUMN receiver_id BIGINT NOT NULL COMMENT "接收者ID"', 
    'SELECT "receiver_id列已存在，跳过添加" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 更新null或0的receiver_id
UPDATE t_chat_message m 
JOIN t_conversation c ON m.conversation_id = c.conversation_id 
SET m.receiver_id = CASE WHEN m.sender_id = c.user_a_id THEN c.user_b_id ELSE c.user_a_id END 
WHERE m.receiver_id = 0 OR m.receiver_id IS NULL;

-- 检查并重命名type字段为message_type
SET @type_exists = 0;
SET @message_type_exists = 0;
SELECT COUNT(*) INTO @type_exists FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 't_chat_message' AND COLUMN_NAME = 'type' AND TABLE_SCHEMA = DATABASE();
SELECT COUNT(*) INTO @message_type_exists FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 't_chat_message' AND COLUMN_NAME = 'message_type' AND TABLE_SCHEMA = DATABASE();

SET @sql = IF(@type_exists > 0 AND @message_type_exists = 0, 
    'ALTER TABLE t_chat_message CHANGE COLUMN type message_type INT DEFAULT 1 COMMENT "消息类型：1-文本，2-图片"', 
    'SELECT "type字段不存在或message_type字段已存在，跳过重命名" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并重命名status字段为is_read
SET @status_exists = 0;
SET @is_read_exists = 0;
SELECT COUNT(*) INTO @status_exists FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 't_chat_message' AND COLUMN_NAME = 'status' AND TABLE_SCHEMA = DATABASE();
SELECT COUNT(*) INTO @is_read_exists FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 't_chat_message' AND COLUMN_NAME = 'is_read' AND TABLE_SCHEMA = DATABASE();

SET @sql = IF(@status_exists > 0 AND @is_read_exists = 0, 
    'ALTER TABLE t_chat_message CHANGE COLUMN status is_read BOOLEAN DEFAULT FALSE COMMENT "是否已读"', 
    'SELECT "status字段不存在或is_read字段已存在，跳过重命名" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修复NULL值的message_type
UPDATE t_chat_message SET message_type = 1 WHERE message_type IS NULL;

-- 修复NULL值的is_read
UPDATE t_chat_message SET is_read = 0 WHERE is_read IS NULL;

-- 用户资料表结构修复脚本

-- 检查用户资料表中是否有重复用户ID
CREATE TEMPORARY TABLE IF NOT EXISTS temp_duplicate_profiles AS
SELECT user_id, MIN(profile_id) as min_id, MAX(profile_id) as max_id, COUNT(*) as count
FROM t_user_profile
GROUP BY user_id
HAVING COUNT(*) > 1;

-- 保留最新的用户资料记录，删除旧的记录
DELETE p FROM t_user_profile p
JOIN temp_duplicate_profiles t ON p.user_id = t.user_id
WHERE p.profile_id < t.max_id;

-- 检查并添加t_conversation表的unread_count字段
SET @column_exists = 0;
SELECT COUNT(*) INTO @column_exists FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 't_conversation' AND COLUMN_NAME = 'unread_count' AND TABLE_SCHEMA = DATABASE();

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE t_conversation ADD COLUMN unread_count INT DEFAULT 0 COMMENT "未读消息数量"', 
    'SELECT "unread_count列已存在，跳过添加" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 更新会话表的未读消息数量
UPDATE t_conversation c
SET c.unread_count = (
    SELECT COUNT(*) FROM t_chat_message m
    WHERE m.conversation_id = c.conversation_id
    AND m.is_read = 0
)
WHERE c.unread_count = 0 OR c.unread_count IS NULL; 