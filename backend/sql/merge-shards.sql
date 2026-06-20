-- ============================================
-- 合并 256 个分片表数据到单表 images 的批量脚本
-- 在 MySQL 命令行执行：
--   mysql -u root -p photo_management < merge-shards.sql
-- ============================================

-- 生成 INSERT 语句的存储过程
DELIMITER //

CREATE PROCEDURE merge_image_shards()
BEGIN
    DECLARE shard_idx INT DEFAULT 0;
    DECLARE shard_name VARCHAR(20);
    DECLARE sql_cmd TEXT;
    DECLARE table_exists INT DEFAULT 0;
    
    WHILE shard_idx < 256 DO
        SET shard_name = CONCAT('images_', LPAD(LOWER(HEX(shard_idx)), 2, '0'));
        
        -- 检查表是否存在
        SELECT COUNT(*) INTO table_exists FROM information_schema.tables 
        WHERE table_schema = 'photo_management' AND table_name = shard_name;
        
        IF table_exists > 0 THEN
            SET sql_cmd = CONCAT('INSERT IGNORE INTO images SELECT * FROM ', shard_name, ' WHERE deleted = 0');
            SET @stmt = sql_cmd;
            PREPARE stmt FROM @stmt;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
            SELECT CONCAT('Merged: ', shard_name) AS progress;
        END IF;
        
        SET shard_idx = shard_idx + 1;
    END WHILE;
END//

DELIMITER ;

-- 执行合并
CALL merge_image_shards();

-- 删除存储过程
DROP PROCEDURE IF EXISTS merge_image_shards;
