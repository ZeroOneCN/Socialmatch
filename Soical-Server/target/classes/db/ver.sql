-- 认证基础表
CREATE TABLE IF NOT EXISTS `t_verification` (
  `verification_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '认证ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '认证类型：identity-身份认证，education-教育认证',
  `status` varchar(20) NOT NULL COMMENT '认证状态：pending-待审核，approved-已通过，rejected-已拒绝',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NOT NULL COMMENT '认证提交时间',
  `update_time` datetime DEFAULT NULL COMMENT '认证审核时间',
  PRIMARY KEY (`verification_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type_status` (`type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证表';

-- 身份认证表
CREATE TABLE IF NOT EXISTS `t_identity_verification` (
  `verification_id` bigint(20) NOT NULL COMMENT '认证ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '认证类型：identity-身份认证',
  `status` varchar(20) NOT NULL COMMENT '认证状态：pending-待审核，approved-已通过，rejected-已拒绝',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NOT NULL COMMENT '认证提交时间',
  `update_time` datetime DEFAULT NULL COMMENT '认证审核时间',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `id_number` varchar(18) NOT NULL COMMENT '身份证号',
  `id_card_front` varchar(255) NOT NULL COMMENT '身份证正面照片URL',
  `id_card_back` varchar(255) NOT NULL COMMENT '身份证背面照片URL',
  PRIMARY KEY (`verification_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='身份认证表';

-- 教育认证表
CREATE TABLE IF NOT EXISTS `t_education_verification` (
  `verification_id` bigint(20) NOT NULL COMMENT '认证ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '认证类型：education-教育认证',
  `status` varchar(20) NOT NULL COMMENT '认证状态：pending-待审核，approved-已通过，rejected-已拒绝',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NOT NULL COMMENT '认证提交时间',
  `update_time` datetime DEFAULT NULL COMMENT '认证审核时间',
  `school` varchar(100) NOT NULL COMMENT '学校名称',
  `college` varchar(100) DEFAULT NULL COMMENT '学院',
  `major` varchar(100) NOT NULL COMMENT '专业',
  `student_id` varchar(50) NOT NULL COMMENT '学号',
  `enrollment_year` int(4) NOT NULL COMMENT '入学年份',
  `student_card_front` varchar(255) NOT NULL COMMENT '学生证封面照片URL',
  `student_card_inside` varchar(255) NOT NULL COMMENT '学生证内页照片URL',
  PRIMARY KEY (`verification_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教育认证表'; 