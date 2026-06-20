-- ============================================
-- 合并 256 分片表数据到单表 images (跳过 id，自动生成新 ID)
-- ============================================

DELIMITER //

CREATE PROCEDURE merge_image_shards()
BEGIN
    DECLARE shard_idx INT DEFAULT 0;
    DECLARE shard_name VARCHAR(20);
    DECLARE sql_cmd TEXT;
    DECLARE table_exists INT DEFAULT 0;
    DECLARE total_rows INT DEFAULT 0;

    WHILE shard_idx < 256 DO
        SET shard_name = CONCAT('images_', LPAD(LOWER(HEX(shard_idx)), 2, '0'));

        SELECT COUNT(*) INTO table_exists FROM information_schema.tables
        WHERE table_schema = 'photo_management' AND table_name = shard_name;

        IF table_exists > 0 THEN
            SET sql_cmd = CONCAT(
                'INSERT INTO images (gallery_id, sample_id, file_name, file_path, thumbnail_path, file_size, file_type, width, height, hash, dhash, dh_bucket0, dh_bucket1, dh_bucket2, dh_bucket3, feature_vector, deep_feature_vector, description, tags, sort_order, create_by, create_time, update_time, deleted) ',
                'SELECT gallery_id, sample_id, file_name, file_path, thumbnail_path, file_size, file_type, width, height, hash, dhash, dh_bucket0, dh_bucket1, dh_bucket2, dh_bucket3, feature_vector, deep_feature_vector, description, tags, sort_order, create_by, create_time, update_time, deleted ',
                'FROM ', shard_name, ' WHERE deleted = 0'
            );
            SET @stmt = sql_cmd;
            PREPARE stmt FROM @stmt;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
            SELECT ROW_COUNT() INTO total_rows;
            SELECT CONCAT('Merged: ', shard_name, ' (', total_rows, ' rows)') AS progress;
        END IF;

        SET shard_idx = shard_idx + 1;
    END WHILE;
END//

DELIMITER ;

-- 清空单表并执行合并
TRUNCATE TABLE images;
CALL merge_image_shards();

-- 删除存储过程
DROP PROCEDURE IF EXISTS merge_image_shards;

-- 验证结果
SELECT COUNT(*) AS total_images FROM images;
