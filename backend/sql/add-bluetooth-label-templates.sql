USE photo_management;

CREATE TABLE IF NOT EXISTS `bluetooth_label_templates` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `width` INT NOT NULL DEFAULT 58 COMMENT '标签宽度: 58 或 80',
    `fields` MEDIUMTEXT NOT NULL COMMENT '字段配置(JSON数组)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_by` BIGINT DEFAULT NULL COMMENT '修改人ID',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_width` (`width`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='蓝牙标签打印模板表';
