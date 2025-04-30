-- 添加测试用户
-- 密码都是123456加密后的值

-- 确保不重复添加
INSERT INTO t_user (username, phone, password, avatar, gender, status)
SELECT 'testuser1', '13800000001', '$2a$10$ySG2lkvjFHY5O0./CPIE1OI8VJsuKYEzOYzqIa7AJR6sEgSzUFOAm', '', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'testuser1');

INSERT INTO t_user (username, phone, password, avatar, gender, status)
SELECT 'testuser2', '13800000002', '$2a$10$ySG2lkvjFHY5O0./CPIE1OI8VJsuKYEzOYzqIa7AJR6sEgSzUFOAm', '', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'testuser2');

INSERT INTO t_user (username, phone, password, avatar, gender, status)
SELECT 'testuser3', '13800000003', '$2a$10$ySG2lkvjFHY5O0./CPIE1OI8VJsuKYEzOYzqIa7AJR6sEgSzUFOAm', '', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'testuser3');

INSERT INTO t_user (username, phone, password, avatar, gender, status)
SELECT 'testuser4', '13800000004', '$2a$10$ySG2lkvjFHY5O0./CPIE1OI8VJsuKYEzOYzqIa7AJR6sEgSzUFOAm', '', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'testuser4');

-- 为新用户创建资料
INSERT INTO t_user_profile (user_id, nickname, location, occupation)
SELECT u.user_id, u.username, '北京', '软件工程师'
FROM t_user u
WHERE u.username = 'testuser1'
AND NOT EXISTS (SELECT 1 FROM t_user_profile p WHERE p.user_id = u.user_id);

INSERT INTO t_user_profile (user_id, nickname, location, occupation)
SELECT u.user_id, u.username, '上海', '设计师'
FROM t_user u
WHERE u.username = 'testuser2'
AND NOT EXISTS (SELECT 1 FROM t_user_profile p WHERE p.user_id = u.user_id);

INSERT INTO t_user_profile (user_id, nickname, location, occupation)
SELECT u.user_id, u.username, '广州', '市场专员'
FROM t_user u
WHERE u.username = 'testuser3'
AND NOT EXISTS (SELECT 1 FROM t_user_profile p WHERE p.user_id = u.user_id);

INSERT INTO t_user_profile (user_id, nickname, location, occupation)
SELECT u.user_id, u.username, '深圳', '产品经理'
FROM t_user u
WHERE u.username = 'testuser4'
AND NOT EXISTS (SELECT 1 FROM t_user_profile p WHERE p.user_id = u.user_id); 