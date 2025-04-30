-- 为没有资料的用户创建默认资料
INSERT INTO t_user_profile (user_id, nickname)
SELECT u.user_id, u.username
FROM t_user u
LEFT JOIN t_user_profile p ON u.user_id = p.user_id
WHERE p.profile_id IS NULL; 