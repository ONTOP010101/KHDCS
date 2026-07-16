-- client_samples 增加标签模板ID列表列
ALTER TABLE `client_samples` ADD COLUMN `label_template_ids` TEXT DEFAULT NULL COMMENT '标签模板ID列表(逗号分隔)' AFTER `discount`;
