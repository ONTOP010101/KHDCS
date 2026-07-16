-- 择样明细表（关联择样单和样品资料）
CREATE TABLE IF NOT EXISTS `client_sample_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code_name` VARCHAR(20) NOT NULL COMMENT '本次代号 (关联client_samples.code_name)',
  `sample_id` BIGINT NOT NULL COMMENT '样品ID (关联samples.id)',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 (0=正常,1=已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_code_name` (`code_name`),
  KEY `idx_sample_id` (`sample_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='择样明细表';
