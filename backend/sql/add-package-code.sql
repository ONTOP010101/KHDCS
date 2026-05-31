-- 新增包装编号字段 package_code
-- 用于独立存储「包装编号」数据，与「中文包装」(packaging_cn) 分开
ALTER TABLE samples ADD COLUMN package_code VARCHAR(50) DEFAULT NULL COMMENT '包装编号' AFTER packaging_en;
