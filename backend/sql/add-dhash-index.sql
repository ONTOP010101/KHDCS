DELIMITER $$

CREATE PROCEDURE add_dhash_indexes()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE j INT DEFAULT 0;
    DECLARE hex_chars CHAR(16) DEFAULT '0123456789abcdef';
    DECLARE prefix CHAR(2);
    DECLARE tbl_name VARCHAR(64);
    DECLARE idx_name VARCHAR(64);
    DECLARE done INT DEFAULT 0;

    WHILE i < 16 DO
        WHILE j < 16 DO
            SET prefix = CONCAT(SUBSTRING(hex_chars, i + 1, 1), SUBSTRING(hex_chars, j + 1, 1));
            SET tbl_name = CONCAT('images_', prefix);
            SET idx_name = CONCAT('idx_dhash_', prefix);

            SET @sql = CONCAT('ALTER TABLE ', tbl_name, ' ADD INDEX ', idx_name, ' (dhash)');
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            SET j = j + 1;
        END WHILE;
        SET j = 0;
        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

CALL add_dhash_indexes();
DROP PROCEDURE add_dhash_indexes;

SELECT 'All 256 dhash indexes created successfully' AS result;
