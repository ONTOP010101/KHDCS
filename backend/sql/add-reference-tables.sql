-- 产品种类对照表（支持一级和二级类目）
CREATE TABLE IF NOT EXISTS product_categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL COMMENT '种类编号',
    name VARCHAR(100) NOT NULL COMMENT '种类名称',
    level INT NOT NULL DEFAULT 1 COMMENT '层级：1=一级，2=二级',
    parent_code VARCHAR(20) DEFAULT NULL COMMENT '父级编号（二级关联一级）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_pc_code ON product_categories(code) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_pc_parent ON product_categories(parent_code) WHERE deleted = 0;
