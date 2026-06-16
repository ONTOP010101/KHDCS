CREATE DATABASE IF NOT EXISTS photo_management
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE photo_management;

CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码(BCrypt)',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像路径',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `department` VARCHAR(100) DEFAULT NULL COMMENT '部门',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_department` (`department`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `permissions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(50) NOT NULL COMMENT '权限编码',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型 1菜单 2按钮',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

CREATE TABLE IF NOT EXISTS `role_permissions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS `user_roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `login_logs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
    `browser` VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
    `os` VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1成功 0失败',
    `message` VARCHAR(200) DEFAULT NULL COMMENT '提示信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志';

CREATE TABLE IF NOT EXISTS `operation_logs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '模块',
    `action` VARCHAR(50) DEFAULT NULL COMMENT '操作',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    `params` TEXT DEFAULT NULL COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `duration` INT DEFAULT NULL COMMENT '耗时(ms)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1成功 0失败',
    `message` VARCHAR(200) DEFAULT NULL COMMENT '提示信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志';

CREATE TABLE IF NOT EXISTS `manufacturers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `manufacturer_code` VARCHAR(50) DEFAULT NULL COMMENT '厂商编号',
    `name` VARCHAR(200) DEFAULT NULL COMMENT '厂商名称',
    `booth_no` VARCHAR(50) DEFAULT NULL COMMENT '摊位号',
    `phone1` VARCHAR(20) DEFAULT NULL COMMENT '电话1',
    `mobile1` VARCHAR(20) DEFAULT NULL COMMENT '手机1',
    `contact1` VARCHAR(50) DEFAULT NULL COMMENT '联系人1',
    `visitor_mobile` VARCHAR(20) DEFAULT NULL COMMENT '见客手机',
    `phone2` VARCHAR(20) DEFAULT NULL COMMENT '电话2',
    `mobile2` VARCHAR(20) DEFAULT NULL COMMENT '手机2',
    `contact2` VARCHAR(50) DEFAULT NULL COMMENT '联系人2',
    `address` VARCHAR(500) DEFAULT NULL COMMENT '地址',
    `phone3` VARCHAR(20) DEFAULT NULL COMMENT '电话3',
    `mobile3` VARCHAR(20) DEFAULT NULL COMMENT '手机3',
    `qq` VARCHAR(20) DEFAULT NULL COMMENT 'QQ',
    `other_remark` VARCHAR(500) DEFAULT NULL COMMENT '其他备注',
    `certificate` VARCHAR(200) DEFAULT NULL COMMENT '厂商证书',
    `sms_number` VARCHAR(300) DEFAULT NULL COMMENT '短信号码',
    `booth_meters` VARCHAR(50) DEFAULT NULL COMMENT '摊位米数',
    `booth_type` VARCHAR(50) DEFAULT NULL COMMENT '摊位类型',
    `floor_area` VARCHAR(50) DEFAULT NULL COMMENT '楼层区位',
    `booth_area` VARCHAR(50) DEFAULT NULL COMMENT '摊位区位',
    `last_expiry` VARCHAR(50) DEFAULT NULL COMMENT '上次到期',
    `expiry_date` VARCHAR(50) DEFAULT NULL COMMENT '到期日期',
    `main_card` VARCHAR(50) DEFAULT NULL COMMENT '主卡',
    `sub_card` VARCHAR(50) DEFAULT NULL COMMENT '副卡',
    `registrant` VARCHAR(50) DEFAULT NULL COMMENT '登记人',
    `modifier` VARCHAR(50) DEFAULT NULL COMMENT '修改人',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_manufacturer_code` (`manufacturer_code`),
    KEY `idx_name` (`name`),
    KEY `idx_booth_no` (`booth_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='厂商资料表';

CREATE TABLE IF NOT EXISTS `samples` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sample_code` VARCHAR(50) NOT NULL COMMENT '样品编号',
    `sample_name` VARCHAR(200) NOT NULL COMMENT '样品名称',
    `category` VARCHAR(100) DEFAULT NULL COMMENT '分类',
    `color` VARCHAR(50) DEFAULT NULL COMMENT '颜色',
    `size` VARCHAR(100) DEFAULT NULL COMMENT '尺寸',
    `origin` VARCHAR(100) DEFAULT NULL COMMENT '产地',
    `supplier` VARCHAR(200) DEFAULT NULL COMMENT '供应商',
    `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0停用',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sample_code` (`sample_code`),
    KEY `idx_sample_name` (`sample_name`),
    KEY `idx_category` (`category`),
    KEY `idx_supplier` (`supplier`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='样品资料表';

CREATE TABLE IF NOT EXISTS `gallery` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '图库标题',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图路径',
    `category` VARCHAR(100) DEFAULT NULL COMMENT '分类',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签(逗号分隔)',
    `image_count` INT NOT NULL DEFAULT 0 COMMENT '图片数量',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0停用',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_title` (`title`),
    KEY `idx_category` (`category`),
    KEY `idx_create_by` (`create_by`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='择样图库表';

CREATE TABLE IF NOT EXISTS `images` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `gallery_id` BIGINT DEFAULT NULL COMMENT '所属图库ID',
    `sample_id` BIGINT DEFAULT NULL COMMENT '关联样品ID',
    `file_name` VARCHAR(200) NOT NULL COMMENT '原始文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
    `thumbnail_path` VARCHAR(500) DEFAULT NULL COMMENT '缩略图路径',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
    `width` INT DEFAULT NULL COMMENT '图片宽度',
    `height` INT DEFAULT NULL COMMENT '图片高度',
    `hash` VARCHAR(64) DEFAULT NULL COMMENT '文件hash(用于去重)',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '图片描述',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签(逗号分隔)',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_by` BIGINT DEFAULT NULL COMMENT '上传人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_gallery_id` (`gallery_id`),
    KEY `idx_sample_id` (`sample_id`),
    KEY `idx_hash` (`hash`),
    KEY `idx_create_by` (`create_by`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片表';

CREATE TABLE IF NOT EXISTS `friendships` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `friend_id` BIGINT NOT NULL COMMENT '好友ID',
    `remark` VARCHAR(50) DEFAULT NULL COMMENT '好友备注',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 2拉黑',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_friend_id` (`friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

CREATE TABLE IF NOT EXISTS `friend_requests` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `from_user_id` BIGINT NOT NULL COMMENT '申请人ID',
    `to_user_id` BIGINT NOT NULL COMMENT '目标用户ID',
    `message` VARCHAR(200) DEFAULT NULL COMMENT '申请消息',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待处理 1已同意 2已拒绝',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_from_user` (`from_user_id`),
    KEY `idx_to_user` (`to_user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表';

CREATE TABLE IF NOT EXISTS `chat_messages` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `conversation_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT DEFAULT NULL COMMENT '接收者ID(私聊)',
    `group_id` BIGINT DEFAULT NULL COMMENT '群聊ID',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型 1文字 2图片 3文件',
    `content` TEXT DEFAULT NULL COMMENT '消息内容',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    `file_name` VARCHAR(200) DEFAULT NULL COMMENT '文件名',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读 0未读 1已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_conversation` (`conversation_id`),
    KEY `idx_sender` (`sender_id`),
    KEY `idx_group` (`group_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

CREATE TABLE IF NOT EXISTS `chat_groups` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '群名称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '群头像',
    `owner_id` BIGINT NOT NULL COMMENT '群主ID',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '群描述',
    `max_members` INT NOT NULL DEFAULT 200 COMMENT '最大成员数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0解散',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群聊表';

CREATE TABLE IF NOT EXISTS `group_members` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `group_id` BIGINT NOT NULL COMMENT '群ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色 0成员 1管理员 2群主',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '群昵称',
    `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群成员表';

INSERT INTO `permissions` (`name`, `code`, `type`, `parent_id`, `sort_order`) VALUES
('用户管理', 'users', 1, 0, 1),
('角色管理', 'roles', 1, 0, 2),
('系统日志', 'logs', 1, 0, 3),
('样品资料', 'sample', 1, 0, 4),
('择样图库', 'gallery', 1, 0, 5),
('好友列表', 'friends', 1, 0, 6);

INSERT INTO `roles` (`name`, `code`, `description`) VALUES
('超级管理员', 'admin', '拥有所有权限'),
('普通用户', 'user', '基本操作权限');

INSERT INTO `role_permissions` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
(2, 4), (2, 5), (2, 6);

INSERT INTO `users` (`username`, `password`, `real_name`, `department`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', '管理部', 1);

INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
(1, 1);
