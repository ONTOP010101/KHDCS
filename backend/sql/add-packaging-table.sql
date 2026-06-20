-- 包装方式对照表
CREATE TABLE IF NOT EXISTS packaging_methods (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL COMMENT '包装编号',
    name VARCHAR(100) NOT NULL COMMENT '中文包装',
    name_en VARCHAR(200) DEFAULT NULL COMMENT '英文包装',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_pm_code ON packaging_methods(code) WHERE deleted = 0;
