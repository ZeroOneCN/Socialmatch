-- 查找重复的用户资料记录
SELECT user_id, COUNT(*) as count 
FROM t_user_profile 
GROUP BY user_id 
HAVING COUNT(*) > 1;

-- 查找重复记录的详细信息
SELECT * FROM t_user_profile 
WHERE user_id IN (
    SELECT user_id FROM t_user_profile 
    GROUP BY user_id 
    HAVING COUNT(*) > 1
)
ORDER BY user_id, create_time;

-- 保留每个用户的最新资料记录，删除旧的重复记录
DELETE t1 FROM t_user_profile t1
JOIN t_user_profile t2 ON t1.user_id = t2.user_id
WHERE t1.profile_id < t2.profile_id;

-- 确认问题已解决
SELECT user_id, COUNT(*) as count 
FROM t_user_profile 
GROUP BY user_id 
HAVING COUNT(*) > 1; 