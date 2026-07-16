USE photo_management;

CREATE TABLE IF NOT EXISTS `report_templates` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `title` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '备注描述',
    `template_data` MEDIUMTEXT NOT NULL COMMENT '模板完整数据(JSON)',
    `type` VARCHAR(50) DEFAULT NULL COMMENT '模板类型: client-items(客户择样) / samples(样品资料)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    KEY `idx_title` (`title`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表模板表';

-- 为已有表添加 type 列
ALTER TABLE `report_templates` ADD COLUMN IF NOT EXISTS `type` VARCHAR(50) DEFAULT NULL COMMENT '模板类型: client-items(客户择样) / samples(样品资料)' AFTER `template_data`;
