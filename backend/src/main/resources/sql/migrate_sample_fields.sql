-- ============================================================
-- 样品表(samples)字段重命名迁移
-- 将 /sample 页面的厂商字段与 /manufacturer 统一命名
-- 请先备份数据库再执行
-- 日期: 2026-07-12
-- ============================================================

-- 1. 重命名列
ALTER TABLE samples CHANGE COLUMN supplier name VARCHAR(200) DEFAULT NULL COMMENT '厂商名称';
ALTER TABLE samples CHANGE COLUMN contact_person contact1 VARCHAR(200) DEFAULT NULL COMMENT '联系人';
ALTER TABLE samples CHANGE COLUMN contact_phone phone1 VARCHAR(50) DEFAULT NULL COMMENT '电话';
ALTER TABLE samples CHANGE COLUMN mobile mobile1 VARCHAR(50) DEFAULT NULL COMMENT '手机';
ALTER TABLE samples CHANGE COLUMN sms sms_number VARCHAR(50) DEFAULT NULL COMMENT '短信号码';

-- 2. 重建索引（删除旧索引，创建新索引）
DROP INDEX IF EXISTS idx_samples_supplier ON samples;
DROP INDEX IF EXISTS idx_samples_contact_person ON samples;
CREATE INDEX idx_samples_name ON samples(name);
CREATE INDEX idx_samples_contact1 ON samples(contact1);
