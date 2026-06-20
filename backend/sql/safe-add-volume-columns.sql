-- 安全添加外箱材积和外箱体积列（仅当不存在时添加）
-- 不会重置任何数据，可放心执行

ALTER TABLE `samples` 
    ADD COLUMN IF NOT EXISTS `carton_material_volume` DECIMAL(12,4) DEFAULT NULL COMMENT '外箱材积' AFTER `carton_height`;

ALTER TABLE `samples` 
    ADD COLUMN IF NOT EXISTS `carton_volume` DECIMAL(12,4) DEFAULT NULL COMMENT '外箱体积' AFTER `carton_material_volume`;
