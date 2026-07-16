-- 入库管理 - 代号组主表
CREATE TABLE IF NOT EXISTS `inventory_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code_name` VARCHAR(50) NOT NULL COMMENT '本次代号',
    `image` VARCHAR(500) DEFAULT NULL COMMENT '图片',
    `company_code` VARCHAR(50) DEFAULT NULL COMMENT '公司编号',
    `factory_no` VARCHAR(50) DEFAULT NULL COMMENT '出厂货号',
    `sample_name` VARCHAR(200) DEFAULT NULL COMMENT '样品名称',
    `chinese_package` VARCHAR(200) DEFAULT NULL COMMENT '中文包装',
    `booth_number` VARCHAR(50) DEFAULT NULL COMMENT '摊位号',
    `manufacturer_name` VARCHAR(200) DEFAULT NULL COMMENT '厂商名称',
    `mobile` VARCHAR(50) DEFAULT NULL COMMENT '手机',
    `telephone` VARCHAR(50) DEFAULT NULL COMMENT '电话',
    `manufacturer_code` VARCHAR(50) DEFAULT NULL COMMENT '厂商编号',
    `floor` VARCHAR(20) DEFAULT NULL COMMENT '楼层',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_name` (`code_name`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库管理-代号组主表';
