-- 检查列是否存在，如果不存在则添加
SET @dbname = 'soical';
SET @tablename = 't_conversation';
SET @columnname = 'unread_count';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1', -- 列已存在，不做任何操作
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT DEFAULT 0 COMMENT ''未读消息数量''')
));

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 更新已有会话的未读计数为0
UPDATE t_conversation SET unread_count = 0 WHERE unread_count IS NULL; 