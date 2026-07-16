-- 报价设置表：添加 type 字段区分报价1/报价2
ALTER TABLE `client_sample_price_settings` ADD COLUMN `type` VARCHAR(2) NOT NULL DEFAULT '1' AFTER `code_name`;
-- 删除旧的索引
ALTER TABLE `client_sample_price_settings` DROP INDEX `code_name`;
-- 添加新的唯一索引
ALTER TABLE `client_sample_price_settings` ADD UNIQUE KEY `uk_code_name_type` (`code_name`, `type`);

-- 明细表：添加 calculated_price_2 列
ALTER TABLE `client_sample_items` ADD COLUMN `calculated_price_2` DECIMAL(18,4) NULL COMMENT '报出价2' AFTER `calculated_price`;
