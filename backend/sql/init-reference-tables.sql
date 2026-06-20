-- 产品种类对照表（支持一级和二级类目）
CREATE TABLE IF NOT EXISTS product_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL COMMENT '种类编号',
    name VARCHAR(100) NOT NULL COMMENT '种类名称',
    level INT NOT NULL DEFAULT 1 COMMENT '层级：1=一级，2=二级',
    parent_code VARCHAR(20) DEFAULT NULL COMMENT '父级编号（二级关联一级）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    UNIQUE KEY idx_pc_code (code, deleted)
);
CREATE INDEX idx_pc_parent ON product_categories(parent_code);

-- 包装方式对照表
CREATE TABLE IF NOT EXISTS packaging_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL COMMENT '包装编号',
    name VARCHAR(100) NOT NULL COMMENT '中文包装',
    name_en VARCHAR(200) DEFAULT NULL COMMENT '英文包装',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    UNIQUE KEY idx_pm_code (code, deleted)
);

-- 给样品表新增 category_code 列
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'samples' AND COLUMN_NAME = 'category_code');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE samples ADD COLUMN category_code VARCHAR(50) DEFAULT NULL COMMENT ''种类编号'' AFTER category', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
