-- 给样品表新增 category_code 列，独立存储种类编号
ALTER TABLE samples ADD COLUMN IF NOT EXISTS category_code VARCHAR(50) DEFAULT NULL COMMENT '种类编号' AFTER category;
