CREATE TABLE IF NOT EXISTS `t_system_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group` varchar(50) NOT NULL COMMENT '设置组（如security, basic, notification等）',
  `setting_key` varchar(50) NOT NULL COMMENT '设置键',
  `setting_value` text COMMENT '设置值（可能是字符串、数字、JSON等）',
  `description` varchar(255) DEFAULT NULL COMMENT '设置描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_key` (`group`, `setting_key`) COMMENT '组和键的唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统设置表';

-- 插入安全设置的默认值
INSERT INTO `t_system_settings` (`group`, `setting_key`, `setting_value`, `description`) VALUES
-- 基础设置组
('basic', 'siteName', 'Soical社交平台', '网站名称'),
('basic', 'siteTitle', 'Soical - 年轻人的社交选择', '网站标题'),
('basic', 'siteDescription', 'Soical是一个年轻人的社交平台，提供多样化的社交功能和良好的用户体验。', '网站描述'),
('basic', 'siteLogo', 'https://placeholder.com/150x50', '网站Logo URL'),
('basic', 'icpNumber', '京ICP备12345678号', 'ICP备案号'),
('basic', 'contactPhone', '400-123-4567', '联系电话'),
('basic', 'contactEmail', 'support@soical.com', '联系邮箱'),
('basic', 'contactQQ', '123456789', '客服QQ'),

-- 安全设置组
('security', 'passwordMinLength', '8', '密码最小长度'),
('security', 'passwordStrength', '["lowercase","number"]', '密码强度要求'),
('security', 'passwordExpireDays', '90', '密码过期天数'),
('security', 'maxLoginAttempts', '5', '最大登录尝试次数'),
('security', 'accountLockTime', '30', '账户锁定时间（分钟）'),
('security', 'enableCaptcha', 'true', '是否启用验证码'),
('security', 'sessionTimeout', '30', '会话超时时间（分钟）'),

-- 备份设置组
('backup', 'enableAutoBackup', 'true', '是否启用自动备份'),
('backup', 'frequency', 'daily', '备份频率（daily/weekly/monthly）'),
('backup', 'backupTime', '02:00', '备份时间 (HH:mm)'),
('backup', 'keepBackups', '7', '保留备份数量');