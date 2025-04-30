-- 这个脚本用于手动测试向聊天消息表插入数据
-- 请确保替换下面的用户ID和会话ID为实际存在的值

-- 首先查询现有会话
SELECT * FROM t_conversation LIMIT 5;

-- 获取两个用户ID
SET @user1 = (SELECT user_a_id FROM t_conversation ORDER BY conversation_id LIMIT 1);
SET @user2 = (SELECT user_b_id FROM t_conversation ORDER BY conversation_id LIMIT 1);
SET @conversation_id = (SELECT conversation_id FROM t_conversation ORDER BY conversation_id LIMIT 1);

-- 显示我们将使用的值
SELECT @user1 AS user1, @user2 AS user2, @conversation_id AS conversation_id;

-- 尝试插入测试消息
INSERT INTO t_chat_message (
    conversation_id, 
    sender_id, 
    receiver_id, 
    content, 
    message_type, 
    is_read, 
    create_time
) VALUES (
    @conversation_id,  -- 会话ID
    @user1,            -- 发送者ID
    @user2,            -- 接收者ID
    '这是一条测试消息',  -- 消息内容
    1,                 -- 消息类型：1-文本
    0,                 -- 是否已读：0-未读
    NOW()              -- 创建时间
);

-- 检查消息是否已插入
SELECT * FROM t_chat_message WHERE content = '这是一条测试消息'; 