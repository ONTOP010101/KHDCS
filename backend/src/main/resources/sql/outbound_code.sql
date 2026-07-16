-- 出库管理 - 代号表
CREATE TABLE IF NOT EXISTS `outbound_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `outbound_code` VARCHAR(50) DEFAULT NULL COMMENT '出库编号',
    `code_name` VARCHAR(50) NOT NULL COMMENT '本次代号',
    `create_date` VARCHAR(50) DEFAULT NULL COMMENT '创建日期',
    `creator` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `floor` VARCHAR(20) DEFAULT NULL COMMENT '楼层',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_name` (`code_name`),
    INDEX `idx_outbound_code` (`outbound_code`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库管理-代号表';
