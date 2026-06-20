-- ============================================
-- 数据库迁移脚本：256分片表 → 单表 images
-- 注意：此脚本假设测试数据不重要，可直接重建
-- ============================================

-- 1. 创建单表 images（如果不存在）
CREATE TABLE IF NOT EXISTS images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gallery_id BIGINT DEFAULT NULL,
    sample_id BIGINT DEFAULT NULL,
    file_name VARCHAR(255) DEFAULT NULL,
    file_path VARCHAR(500) DEFAULT NULL,
    thumbnail_path VARCHAR(500) DEFAULT NULL,
    file_size BIGINT DEFAULT NULL,
    file_type VARCHAR(20) DEFAULT NULL,
    width INT DEFAULT NULL,
    height INT DEFAULT NULL,
    hash VARCHAR(64) NOT NULL,
    dhash BIGINT DEFAULT NULL,
    dh_bucket_0 INT DEFAULT NULL,
    dh_bucket_1 INT DEFAULT NULL,
    dh_bucket_2 INT DEFAULT NULL,
    dh_bucket_3 INT DEFAULT NULL,
    feature_vector MEDIUMBLOB DEFAULT NULL,
    deep_feature_vector MEDIUMBLOB DEFAULT NULL,
    description TEXT DEFAULT NULL,
    tags VARCHAR(500) DEFAULT NULL,
    sort_order INT DEFAULT 0,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    
    UNIQUE KEY uk_hash (hash),
    KEY idx_sample_id (sample_id),
    KEY idx_gallery_id (gallery_id),
    KEY idx_dhash (dhash),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 合并256个分片表数据到单表（按需执行，留空由管理脚本动态生成）
-- INSERT INTO images SELECT * FROM images_00 WHERE deleted = 0;
-- INSERT INTO images SELECT * FROM images_01 WHERE deleted = 0;
-- ... (共256条)
-- INSERT INTO images SELECT * FROM images_ff WHERE deleted = 0;

-- 3. 合并完成后删除256个分片表（按需执行）
-- DROP TABLE IF EXISTS images_00;
-- DROP TABLE IF EXISTS images_01;
-- ... (共256条)
-- DROP TABLE IF EXISTS images_ff;
